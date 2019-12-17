/*
 * $Id$
 *
 * SARL is an general-purpose agent programming language.
 * More details on http://www.sarl.io
 *
 * Copyright (C) 2014-2019 the original authors or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.janusproject.kernel.bic;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

import com.google.inject.Inject;

import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

import io.janusproject.services.contextspace.ContextSpaceService;
import io.sarl.core.InnerContextAccess;
import io.sarl.core.OpenEventSpace;
import io.sarl.lang.core.Address;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.EventListener;
import io.sarl.lang.core.Skill;
import io.sarl.lang.core.Space;
import io.sarl.lang.core.SpaceID;
import io.sarl.lang.util.ClearableReference;

/**
 * Janus implementation of SARL's {@link InnerContextSkill} built-in capacity.
 *
 * @author $Author: srodriguez$
 * @author $Author: ngaud$
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class InnerContextSkill extends BuiltinSkill implements InnerContextAccess {

	private static int installationOrder = -1;

	private final Address agentAddressInInnerDefaultSpace;

	private ClearableReference<Skill> skillBufferInternalEventBusCapacity;

	/**
	 * Context inside the agent.
	 */
	private AgentContext innerContext;

	private ContextSpaceService contextService;

	/**
	 * Constructor.
	 *
	 * @param agent                           owner of this skill.
	 * @param agentAddressInInnerDefaultSpace address of the owner of this skill in
	 *                                        its default space.
	 */
	InnerContextSkill(Agent agent, Address agentAddressInInnerDefaultSpace) {
		super(agent);
		this.agentAddressInInnerDefaultSpace = agentAddressInInnerDefaultSpace;
	}

	/**
	 * Change the reference to the service that is managing the contexts and the
	 * spaces.
	 *
	 * @param contextService the service.
	 */
	@Inject
	public final void setContextSpaceService(ContextSpaceService contextService) {
		this.contextService = contextService;
	}

	/**
	 * Replies the InternalEventBusCapacity skill as fast as possible.
	 *
	 * @return the skill
	 */
	protected final InternalEventBusCapacity getInternalEventBusCapacitySkill() {
		if (this.skillBufferInternalEventBusCapacity == null
				|| this.skillBufferInternalEventBusCapacity.get() == null) {
			this.skillBufferInternalEventBusCapacity = $getSkill(InternalEventBusCapacity.class);
		}
		return $castSkill(InternalEventBusCapacity.class, this.skillBufferInternalEventBusCapacity);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated since 0.10
	 */
	@Override
	@Deprecated
	public int getInstallationOrder() {
		if (installationOrder < 0) {
			installationOrder = installationOrder(this);
		}
		return installationOrder;
	}

	/**
	 * Replies if the inner context was instanciated. To create the inner context,
	 * call {@link #getInnerContext()}
	 *
	 * @return <code>true</code> if an instance of inner context exists, otherwise
	 *         <code>false</code>.
	 */
	public boolean hasInnerContext() {
		return getInnerContextWithoutAutomaticCreation() != null;
	}

	/**
	 * Force to reset the inner context. This function does not update the context
	 * repository.
	 *
	 * <p>Do not call this function, exception if you are sure that the setting of the
	 * inner context to <code>null</code> only does not introduce problems.
	 */
	void resetInnerContext() {
		// synchronized (this.innerContext) {
		this.innerContext = null;
		// }
	}

	@Override
	@Pure
	public void toString(ToStringBuilder builder) {
		super.toString(builder);
		builder.add("addressInDefaultspace", this.agentAddressInInnerDefaultSpace); //$NON-NLS-1$
	}

	@Override
	protected void uninstall(UninstallationStage stage) {
		if (stage == UninstallationStage.POST_DESTROY_EVENT) {
			final AgentContext context;
			// synchronized (this.innerContext) {
			context = this.innerContext;
			this.innerContext = null;
			// }
			if (context != null) {
				// Unregister the agent from the default space
				final EventListener listener = getInternalEventBusCapacitySkill().asEventListener();
				((OpenEventSpace) context.getDefaultSpace()).unregister(listener);
				// Destroy the context
				this.contextService.removeContext(context);
			}
		}
	}

	/**
	 * Replies the inner context, but do not create one if it was not created before
	 * calling this function. If you would like to automatically create the inner
	 * context, please call {@link #getInnerContext()}.
	 *
	 * <p>This function is thread-safe.
	 *
	 * @return the inner context, or {@code null}.
	 * @since 0.10
	 * @see #getInnerContext()
	 */
	protected final AgentContext getInnerContextWithoutAutomaticCreation() {
		// synchronized (this.innerContext) {
		return this.innerContext;
		// }
	}

	@Override
	public AgentContext getInnerContext() {
		AgentContext ictx;
		// synchronized (this.innerContext) {
		ictx = this.innerContext;
		// }
		if (ictx == null) {
			// synchronized (this.innerContext) {
			// Create the inner context.
			this.innerContext = this.contextService.createContext(
					this.agentAddressInInnerDefaultSpace.getSpaceID().getContextID(),
					this.agentAddressInInnerDefaultSpace.getSpaceID().getID());
			ictx = this.innerContext;
			// Register the agent in the default space
			final EventListener listener = getInternalEventBusCapacitySkill().asEventListener();
			final OpenEventSpace defSpace = (OpenEventSpace) ictx.getDefaultSpace();
			defSpace.register(listener);
			// }
		}
		return ictx;
	}

	@Override
	public boolean hasMemberAgent() {
		final AgentContext ictx = getInnerContextWithoutAutomaticCreation();
		if (ictx != null) {
			final Set<UUID> participants = ictx.getDefaultSpace().getParticipants();
			assert participants != null;
			return (participants.size() > 1)
					|| ((participants.size() == 1) && (!participants.contains(getOwner().getID())));
		}
		return false;
	}

	@Override
	public int getMemberAgentCount() {
		final AgentContext ictx = getInnerContextWithoutAutomaticCreation();
		if (ictx != null) {
			final Set<UUID> participants = ictx.getDefaultSpace().getParticipants();
			assert participants != null;

			int count = participants.size();
			if (participants.contains(getOwner().getID())) {
				--count;
			}
			return count;
		}
		return 0;
	}

	@Override
	public ConcurrentSkipListSet<UUID> getMemberAgents() {
		ConcurrentSkipListSet<UUID> participants = null;
		final AgentContext ictx = getInnerContextWithoutAutomaticCreation();
		if (ictx != null) {
			participants = ictx.getDefaultSpace().getParticipants();
			assert participants != null;
		}
		ConcurrentSkipListSet<UUID> members = null;
		if (participants != null) {
			members = new ConcurrentSkipListSet<>();
			final UUID myId = getOwner().getID();

			for (final UUID id : participants) {
				if (!id.equals(myId)) {
					members.add(id);
				}
			}
		}
		if (members != null) {
			return members;
		}
		return new ConcurrentSkipListSet<UUID>();
	}

	@Override
	public boolean isInnerDefaultSpace(Space space) {
		return isInnerDefaultSpace(space.getSpaceID());
	}

	@Override
	public boolean isInnerDefaultSpace(SpaceID spaceID) {
		final AgentContext context = getInnerContextWithoutAutomaticCreation();
		return context != null && spaceID.equals(context.getDefaultSpace().getSpaceID());
	}

	@Override
	public boolean isInnerDefaultSpace(UUID spaceID) {
		final AgentContext context = getInnerContextWithoutAutomaticCreation();
		return context != null && spaceID.equals(context.getDefaultSpace().getSpaceID().getID());
	}

	@Override
	public boolean isInInnerDefaultSpace(Event event) {
		if (event != null) {
			final Address adr = event.getSource();
			if (adr != null) {
				return isInnerDefaultSpace(adr.getSpaceID());
			}
		}
		return false;
	}

}
