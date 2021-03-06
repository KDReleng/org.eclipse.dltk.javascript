/*******************************************************************************
 * Copyright (c) 2013 NumberFour AG
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     NumberFour AG - initial API and Implementation (Alex Panchenko)
 *******************************************************************************/
package org.eclipse.dltk.javascript.typeinfo;

import org.eclipse.dltk.javascript.typeinfo.IModelBuilder.IMethod;
import org.eclipse.dltk.javascript.typeinfo.IModelBuilder.IVariable;

/**
 * Listener for events happening during type inferencing.
 * 
 * @see ITypeInferencerVisitor#addListener(ITypeInferenceListener)
 */
public interface ITypeInferenceListener {

	/**
	 * Notifies that variable declaration was parsed.
	 */
	void variableParsed(IVariable variable);

	/**
	 * Notifies that method declaration was parsed.
	 */
	void methodParsed(IMethod method);

	/**
	 * Notifies that type inferencing of the file was completed.
	 */
	void done();

}
