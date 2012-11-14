/*******************************************************************************
 * Copyright (c) 2011 NumberFour AG
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

import org.eclipse.dltk.annotations.NonNull;
import org.eclipse.dltk.javascript.typeinfo.model.Type;

/**
 * Resolved type expression representing <em>simple type reference</em>.
 */
public interface IRSimpleType extends IRType {

	@NonNull
	Type getTarget();

	@NonNull
	IRTypeDeclaration getDeclaration();

}
