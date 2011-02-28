/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package org.eclipse.dltk.javascript.internal.ui.preferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dltk.ast.parser.SourceParserManager;
import org.eclipse.dltk.compiler.problem.DefaultProblemIdentifier;
import org.eclipse.dltk.compiler.problem.IProblemIdentifier;
import org.eclipse.dltk.compiler.problem.ProblemSeverity;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IDLTKContributedExtension;
import org.eclipse.dltk.core.SourceParserUtil;
import org.eclipse.dltk.internal.ui.preferences.ScrolledPageContent;
import org.eclipse.dltk.javascript.core.JavaScriptNature;
import org.eclipse.dltk.javascript.internal.ui.JavaScriptUI;
import org.eclipse.dltk.javascript.parser.JavaScriptParserPlugin;
import org.eclipse.dltk.javascript.parser.JavaScriptParserPreferences;
import org.eclipse.dltk.ui.preferences.AbstractOptionsBlock;
import org.eclipse.dltk.ui.preferences.IPreferenceChangeRebuildPrompt;
import org.eclipse.dltk.ui.preferences.PreferenceChangeRebuildPrompt;
import org.eclipse.dltk.ui.preferences.PreferenceKey;
import org.eclipse.dltk.ui.util.IStatusChangeListener;
import org.eclipse.dltk.ui.util.SWTFactory;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

public class JavaScriptErrorWarningConfigurationBlock extends
		AbstractOptionsBlock {

	public JavaScriptErrorWarningConfigurationBlock(
			IStatusChangeListener context, IProject project,
			IWorkbenchPreferenceContainer container) {
		super(context, project, new PreferenceKey[0], container);
	}

	protected static class ProblemSection {
		final String id;
		final String name;

		public ProblemSection(String id, String name) {
			this.id = id;
			this.name = name;
		}

		final Map<IProblemIdentifier, String> items = new LinkedHashMap<IProblemIdentifier, String>();
	}

	private static final String EXT_POINT_PROBLEM_SECTIONS = JavaScriptUI.PLUGIN_ID
			+ ".problemSections";

	protected List<ProblemSection> loadProblemSections() {
		final List<ProblemSection> sections = new ArrayList<ProblemSection>();
		final IConfigurationElement[] elements = Platform
				.getExtensionRegistry().getConfigurationElementsFor(
						EXT_POINT_PROBLEM_SECTIONS);
		final Set<IProblemIdentifier> identifiers = new HashSet<IProblemIdentifier>();
		for (IConfigurationElement element : elements) {
			final String sectionId = element.getAttribute("id");
			final String sectionName = element.getAttribute("name");
			ProblemSection problemSection = null;
			if (sectionId != null) {
				for (ProblemSection section : sections) {
					if (sectionId.equals(section.id)) {
						problemSection = section;
						break;
					}
				}
			}
			if (problemSection == null) {
				problemSection = new ProblemSection(sectionId, sectionName);
				sections.add(problemSection);
			}
			for (IConfigurationElement problemElement : element
					.getChildren("problem")) {
				final String problemId = problemElement.getAttribute("id");
				final IProblemIdentifier identifier = DefaultProblemIdentifier
						.decode(problemId);
				if (identifier != null && identifiers.add(identifier)) {
					String problemLabel = problemElement.getAttribute("label");
					if (problemLabel == null || problemLabel.length() == 0) {
						problemLabel = identifier.name();
					}
					if (!problemSection.items.containsKey(identifier)) {
						problemSection.items.put(identifier, problemLabel);
					}
				}
			}
		}
		return sections;
	}

	@Override
	public Control createOptionsBlock(Composite parent) {
		final ScrolledPageContent sc1 = new ScrolledPageContent(parent);
		final GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = new PixelConverter(parent)
				.convertHeightInCharsToPixels(20);
		sc1.setLayoutData(gridData);

		Composite composite = sc1.getBody();
		final GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		composite.setFont(parent.getFont());

		if (isProjectPreferencePage()) {
			final IDLTKContributedExtension[] extensions = SourceParserManager
					.getInstance().getContributions(JavaScriptNature.NATURE_ID);
			if (extensions.length > 1) {
				SWTFactory.createLabel(composite, "Parser", 1);
				final String[] ids = new String[extensions.length];
				final String[] names = new String[extensions.length];
				for (int i = 0; i < extensions.length; ++i) {
					ids[i] = extensions[i].getId();
					names[i] = extensions[i].getName();
				}
				bindControl(SWTFactory.createCombo(composite, SWT.READ_ONLY, 1,
						0, names), new PreferenceKey(DLTKCore.PLUGIN_ID,
						DLTKCore.PROJECT_SOURCE_PARSER_ID), ids);
			}
		}

		bindControl(SWTFactory.createCheckButton(composite,
				JavaScriptPreferenceMessages.ErrorWarning_enableTypeInfo, null,
				false, 2), new PreferenceKey(JavaScriptParserPlugin.PLUGIN_ID,
				JavaScriptParserPreferences.ENABLE_TYPE_INFO), null);
		SWTFactory.createLabel(composite,
				JavaScriptPreferenceMessages.ErrorWarningDescription, 2);

		String[] names = new String[] { "Warning", "Error", "Info", "Ignore" };
		String[] ids = new String[] { ProblemSeverity.WARNING.name(),
				ProblemSeverity.ERROR.name(), ProblemSeverity.INFO.name(),
				ProblemSeverity.IGNORE.name() };

		for (ProblemSection problemSection : loadProblemSections()) {
			if (problemSection.items.isEmpty())
				continue;
			final ExpandableComposite excomposite = createStyleSection(
					composite, problemSection.name, 2);
			final Composite inner = new Composite(excomposite, SWT.NONE);
			inner.setFont(composite.getFont());
			inner.setLayout(new GridLayout(2, false));
			excomposite.setClient(inner);
			for (Map.Entry<IProblemIdentifier, String> entry : problemSection.items
					.entrySet()) {
				SWTFactory.createLabel(inner, entry.getValue(), 1)
						.setLayoutData(
								new GridData(GridData.FILL, GridData.CENTER,
										true, false));
				bindControl(SWTFactory.createCombo(inner, SWT.READ_ONLY, 1, 0,
						names), key(entry.getKey()), ids);
			}
			excomposite.setExpanded(true);
		}

		return composite;
	}

	private static PreferenceKey key(IProblemIdentifier identifier) {
		return new PreferenceKey(identifier.contributor(),
				DefaultProblemIdentifier.encode(identifier));
	}

	@Override
	protected IPreferenceChangeRebuildPrompt getPreferenceChangeRebuildPrompt(
			boolean workspaceSettings, Collection<PreferenceKey> changedOptions) {
		return PreferenceChangeRebuildPrompt.create(workspaceSettings,
				"Parser Settings Changed", "The parser settings have changed.");
	}

	@Override
	protected Job[] createBuildJobs(IProject project) {
		final Job[] jobs = super.createBuildJobs(project);
		final Job[] result = new Job[jobs.length + 1];
		System.arraycopy(jobs, 0, result, 1, jobs.length);
		Job job0 = jobs[0];
		result[0] = new Job(job0.getName()) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				SourceParserUtil.clearCache();
				return Status.OK_STATUS;
			}
		};
		result[0].setRule(job0.getRule());
		result[0].setUser(job0.isUser());
		result[0].setSystem(job0.isSystem());
		return result;
	}

}
