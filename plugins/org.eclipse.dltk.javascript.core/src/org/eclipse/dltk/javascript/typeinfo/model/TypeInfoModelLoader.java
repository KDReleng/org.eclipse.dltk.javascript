/*******************************************************************************
 * Copyright (c) 2010 xored software, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
 *******************************************************************************/
package org.eclipse.dltk.javascript.typeinfo.model;

import org.eclipse.dltk.javascript.typeinfo.TypeInfoManager;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

public class TypeInfoModelLoader {

	private static TypeInfoModelLoader instance = null;

	public synchronized static TypeInfoModelLoader getInstance() {
		if (instance == null) {
			instance = new TypeInfoModelLoader();
		}
		return instance;
	}

	private final ResourceSet resourceSet;

	private TypeInfoModelLoader() {
		resourceSet = TypeInfoManager.loadModelResources();
	}

	public Type getType(String typeName) {
		for (Resource resource : resourceSet.getResources()) {
			for (EObject object : resource.getContents()) {
				if (object instanceof Type) {
					final Type type = (Type) object;
					if (typeName.equals(type.getName())) {
						return type;
					}
				}
			}
		}
		return null;
	}

}