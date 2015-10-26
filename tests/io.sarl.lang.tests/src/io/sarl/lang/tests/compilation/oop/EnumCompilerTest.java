/*
 * Copyright (C) 2014-2015 the original authors or authors.
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
package io.sarl.lang.tests.compilation.oop;

import static org.junit.Assert.assertEquals;
import io.sarl.lang.SARLInjectorProvider;
import io.sarl.lang.tests.parsing.aop.BehaviorParsingTest;
import io.sarl.tests.api.AbstractSarlTest;

import org.eclipse.xtext.junit4.InjectWith;
import org.eclipse.xtext.junit4.XtextRunner;
import org.eclipse.xtext.util.IAcceptor;
import org.eclipse.xtext.xbase.compiler.CompilationTestHelper;
import org.eclipse.xtext.xbase.compiler.CompilationTestHelper.Result;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.google.inject.Inject;

/**
 * @author $Author: sgalland$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@RunWith(Suite.class)
@SuiteClasses({
	EnumCompilerTest.TopLevelTest.class,
	EnumCompilerTest.InClassTest.class,
	EnumCompilerTest.InAgentTest.class,
})
@SuppressWarnings("all")
public class EnumCompilerTest {

	public static class TopLevelTest extends AbstractSarlTest {

		@Inject
		private CompilationTestHelper compiler;

		@Test
		public void basic() throws Exception {
			String source = "enum E1 { CST1, CST2 }";
			String expected = multilineString(
					"@SuppressWarnings(\"all\")",
					"public enum E1 {",
					"  CST1,",
					"  ",
					"  CST2;",
					"}",
					""
					);
			this.compiler.assertCompilesTo(source, expected);
		}

	}

	public static class InClassTest extends AbstractSarlTest {

		@Inject
		private CompilationTestHelper compiler;

		@Test
		public void basic() throws Exception {
			String source = "class Container { enum E1 { CST1, CST2 } }";
			String expected = multilineString(
					"@SuppressWarnings(\"all\")",
					"public class Container {",
					"  public enum E1 {",
					"    CST1,",
					"    ",
					"    CST2;",
					"  }",
					"}",
					""
					);
			this.compiler.assertCompilesTo(source, expected);
		}

	}

	public static class InAgentTest extends AbstractSarlTest {

		@Inject
		private CompilationTestHelper compiler;

		@Test
		public void basic() throws Exception {
			String source = "agent Container { enum E1 { CST1, CST2 } }";
			String expected = multilineString(
					"import io.sarl.lang.core.Agent;",
					"import java.util.UUID;",
					"import javax.annotation.Generated;",
					"",
					"@SuppressWarnings(\"all\")",
					"public class Container extends Agent {",
					"  protected enum E1 {",
					"    CST1,",
					"    ",
					"    CST2;",
					"  }",
					"  ",
					"  /**",
					"   * Construct an agent.",
					"   * @param parentID - identifier of the parent. It is the identifier of the parent agent and the enclosing contect, at the same time.",
					"   */",
					"  @Generated(\"io.sarl.lang.jvmmodel.SARLJvmModelInferrer\")",
					"  public Container(final UUID parentID) {",
					"    super(parentID, null);",
					"  }",
					"  ",
					"  /**",
					"   * Construct an agent.",
					"   * @param parentID - identifier of the parent. It is the identifier of the parent agent and the enclosing contect, at the same time.",
					"   * @param agentID - identifier of the agent. If <code>null</code> the agent identifier will be computed randomly.",
					"   */",
					"  @Generated(\"io.sarl.lang.jvmmodel.SARLJvmModelInferrer\")",
					"  public Container(final UUID parentID, final UUID agentID) {",
					"    super(parentID, agentID);",
					"  }",
					"}",
					""
					);
			this.compiler.assertCompilesTo(source, expected);
		}

	}

}
