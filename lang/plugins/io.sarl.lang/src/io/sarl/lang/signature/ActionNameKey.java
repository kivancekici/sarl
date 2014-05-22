/*
 * Copyright 2014 Sebastian RODRIGUEZ, Nicolas GAUD, Stéphane GALLAND.
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
package io.sarl.lang.signature;

import java.io.Serializable;

/**
 * A key for actions names.
 * 
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class ActionNameKey implements Cloneable, Serializable, Comparable<ActionNameKey> {

	private static final long serialVersionUID = -5590450855487852546L;
	
	private String container;
	private String functionName;
	
	/**
	 * @param container
	 * @param functionName
	 */
	protected ActionNameKey(String container, String functionName) {
		this.functionName = functionName;
		this.container = container;
	}
	
	/** Replies the ID of the container.
	 * 
	 * @return the ID.
	 */
	public String getContainerID() {
		return this.container;
	}
	
	/** Replies the name of the function.
	 * 
	 * @return the name.
	 */
	public String getFunctionName() {
		return this.functionName;
	}

	/** {@inheritDoc}
	 */
	@Override
	public ActionNameKey clone() {
		try {
			return (ActionNameKey)super.clone();
		}
		catch (CloneNotSupportedException e) {
			throw new Error(e);
		}
	}
	
	/** {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj==this) return true;
		if (obj instanceof ActionNameKey) {
			ActionNameKey k = (ActionNameKey)obj;
			return this.functionName.equals(k.functionName)
					&& this.container.equals(k.container);
		}
		return false;
	}
	
	/** {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int h = 1;
		h = 31*h + this.functionName.hashCode();
		h = 31*h + this.container.hashCode();
		return h;
	}
	
	/** {@inheritDoc}
	 */
	@Override
	public String toString() {
		return this.container+"#"+this.functionName; //$NON-NLS-1$
	}

	/** {@inheritDoc}
	 */
	public int compareTo(ActionNameKey o) {
		if (o==null) return Integer.MAX_VALUE;
		int cmp = this.container.compareTo(o.container);
		if (cmp!=0) return cmp;
		return this.functionName.compareTo(o.functionName);
	}
	
}