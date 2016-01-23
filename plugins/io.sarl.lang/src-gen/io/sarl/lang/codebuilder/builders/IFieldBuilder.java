/*
 * $Id$
 *
 * File is automatically generated by the Xtext language generator.
 * Do not change it.
 *
 * SARL is an general-purpose agent programming language.
 * More details on http://www.sarl.io
 *
 * Copyright 2014-2016 the original authors and authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.sarl.lang.codebuilder.builders;

import io.sarl.lang.sarl.SarlField;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend.core.xtend.XtendTypeDeclaration;
import org.eclipse.xtext.xbase.lib.Pure;

/** Builder of a Sarl Field.
 */
@SuppressWarnings("all")
public interface IFieldBuilder {

	/** Initialize the Ecore element.
	 * @param container - the container of the Field.
	 * @param name - the name of the Field.
	 */
	void eInit(XtendTypeDeclaration container, String name, String modifier);

	/** Replies the generated element.
	 */
	@Pure
	SarlField getSarlField();

	/** Replies the resource.
	 */
	@Pure
	Resource eResource();

	/** Change the documentation of the element.
	 *
	 * <p>The documentation will be displayed just before the element.
	 *
	 * @param doc the documentation.
	 */
	void setDocumentation(String doc);

	/** Change the type.
	 * @param type the type of the member.
	 */
	void setType(String type);

	/** Change the initialValue.
	 * @param value - the value of the initialValue. It may be <code>null</code>.
	 */
	@Pure
	IExpressionBuilder getInitialValue();

}

