/*
 * $Id$
 *
 * SARL is an general-purpose agent programming language.
 * More details on http://www.sarl.io
 *
 * Copyright (C) 2014 Sebastian RODRIGUEZ, Nicolas GAUD, Stéphane GALLAND.
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
package io.sarl.eclipse.launch.config;

import io.sarl.eclipse.launch.sre.ISREInstall;
import io.sarl.eclipse.launch.sre.ISREInstallChangedListener;
import io.sarl.eclipse.launch.sre.SARLRuntime;
import io.sarl.eclipse.preferences.SREsPreferencePage;
import io.sarl.eclipse.properties.RuntimeEnvironmentPropertyPage;
import io.sarl.eclipse.util.PluginUtil;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.internal.debug.ui.actions.ControlAccessibleListener;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

/**
 * Block of widgets that permits to select and
 * configure a SARL runtime environment.
 *
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class SREConfigurationBlock {

	/** Property name for the SRE configuration.
	 */
	public static final String PROPERTY_SRE_CONFIGURATION = "PROPERTY_SRE_CONFIGURATION"; //$NON-NLS-1$

	/**
	 * This block's control.
	 */
	private Composite control;

	/**
	 * SRE change listeners.
	 */
	private final ListenerList listeners = new ListenerList();

	private final String title;
	
	private ISREInstallChangedListener sreListener;

	private final boolean enableSystemWideSelector;
	private final SRECompliantProjectProvider projectProvider;
	
	private Button systemSREButton;
	private Button projectSREButton;
	private Button specificSREButton;
	
	private final List<ISREInstall> runtimeEnvironments = new ArrayList<>();
	private Combo runtimeEnvironmentCombo;
	private Button runtimeEnvironmentSearchButton;
	
	private boolean notify = true;

	/**
	 * @param enableSystemWideSelector - indicates if the system-wide configuration selector must be enabled.
	 * @param projectProvider - the provider of a project that may give SRE configuration. 
	 */
	public SREConfigurationBlock(boolean enableSystemWideSelector, SRECompliantProjectProvider projectProvider) {
		this(null, enableSystemWideSelector, projectProvider);
	}

	/**
	 * @param title - the title of the group.
	 * @param enableSystemWideSelector - indicates if the system-wide configuration selector must be enabled.
	 * @param projectProvider - the provider of a project that may give SRE configuration. 
	 */
	public SREConfigurationBlock(String title, boolean enableSystemWideSelector, SRECompliantProjectProvider projectProvider) {
		this.title = title;
		this.enableSystemWideSelector = enableSystemWideSelector;
		this.projectProvider = projectProvider;
	}

	/** Change the event notification flag.
	 * 
	 * @param notify - <code>true</code> for notifying the events. 
	 */
	public void setNotify(boolean notify) {
		this.notify = notify;
	}
	
	/** Replies if the events are notified.
	 * 
	 * @return <code>true</code> if the events are notified.
	 */
	public boolean getNotify() {
		return this.notify;
	}
	
	/** Add listener on the changes in the SRE configuration.
	 *
	 * @param listener - the listener.
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		this.listeners.add(listener);
	}

	/** Add listener on the changes in the SRE configuration.
	 *
	 * @param listener - the listener.
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		this.listeners.remove(listener);
	}

	private void firePropertyChange() {
		if (this.notify) {
			PropertyChangeEvent event = new PropertyChangeEvent(this,
					PROPERTY_SRE_CONFIGURATION, null,
					getSelectedSRE());
			Object[] listeners = this.listeners.getListeners();
			for (int i = 0; i < listeners.length; i++) {
				IPropertyChangeListener listener = (IPropertyChangeListener) listeners[i];
				listener.propertyChange(event);
			}
		}
	}
	
	private ISREInstall retreiveProjectSRE() {
		try {
			if (this.projectProvider != null) {
				IProject prj = this.projectProvider.getSRECompliantProject();
				if (prj != null) {
					ISREInstall sre = null;
					QualifiedName propertyName = RuntimeEnvironmentPropertyPage.qualify(
							RuntimeEnvironmentPropertyPage.PROPERTY_NAME_HAS_PROJECT_SPECIFIC);
					if (Boolean.parseBoolean(Objects.firstNonNull(
							prj.getPersistentProperty(propertyName), Boolean.FALSE.toString()))) {
						propertyName = RuntimeEnvironmentPropertyPage.qualify(
								RuntimeEnvironmentPropertyPage.PROPERTY_NAME_USE_SYSTEM_WIDE_SRE);
						boolean useWideConfig = Boolean.parseBoolean(Objects.firstNonNull(
								prj.getPersistentProperty(propertyName), Boolean.FALSE.toString()));
						if (!useWideConfig) {
							propertyName = RuntimeEnvironmentPropertyPage.qualify(
									RuntimeEnvironmentPropertyPage.PROPERTY_NAME_SRE_INSTALL_ID);
							String projectSREId = Strings.nullToEmpty(prj.getPersistentProperty(propertyName));
							sre = SARLRuntime.getSREFromId(projectSREId);
						}
					}
					if (sre == null) {
						sre = SARLRuntime.getDefaultSREInstall();
					}
					return sre;
				}
			}
		} catch (CoreException _) {
			//
		}
		return null;
	}

	private void doSystemSREButtonClick() {
		if (this.specificSREButton != null) {
			this.specificSREButton.setSelection(false);
		}
		if (this.projectSREButton != null) {
			this.projectSREButton.setSelection(false);
		}
		if (this.systemSREButton != null) {
			this.systemSREButton.setSelection(true);
		}
		handleSystemWideConfigurationSelected();
	}
	
	private void doProjectSREButtonClick() {
		if (this.specificSREButton != null) {
			this.specificSREButton.setSelection(false);
		}
		if (this.systemSREButton != null) {
			this.systemSREButton.setSelection(false);
		}
		if (this.projectSREButton != null) {
			this.projectSREButton.setSelection(true);
		}
		handleProjectConfigurationSelected();
	}

	private void doSpecificSREButtonClick() {
		if (this.systemSREButton != null) {
			this.systemSREButton.setSelection(false);
		}
		if (this.projectSREButton != null) {
			this.projectSREButton.setSelection(false);
		}
		if (this.specificSREButton != null) {
			this.specificSREButton.setSelection(true);
		}
		handleSpecificConfigurationSelected();
	}

	/**
	 * Creates this block's control in the given control.
	 *
	 * @param parent - containing control
	 * @return the control.
	 * @see #getControl()
	 */
	public Control createControl(Composite parent) {
		this.control = SWTFactory.createComposite(
				parent, parent.getFont(), 1, 1, GridData.FILL_HORIZONTAL);
		
		int nColumns = this.enableSystemWideSelector ? 3 : 2;
		Group group = SWTFactory.createGroup(this.control,
				Objects.firstNonNull(this.title, Messages.RuntimeEnvironmentTab_1),
				nColumns, 1, GridData.FILL_HORIZONTAL);

		if (this.enableSystemWideSelector || this.projectProvider != null) {
			if (this.enableSystemWideSelector) {
				ISREInstall wideSystemSRE = SARLRuntime.getDefaultSREInstall();
				String wideSystemSRELabel;
				if (wideSystemSRE == null) {
					wideSystemSRELabel = Messages.SREConfigurationBlock_0;
				} else {
					wideSystemSRELabel = wideSystemSRE.getName();
				}
				this.systemSREButton = SWTFactory.createRadioButton(group,
						MessageFormat.format(
								Messages.SREConfigurationBlock_1, wideSystemSRELabel), 3);
				this.systemSREButton.addSelectionListener(new SelectionAdapter() {
					@SuppressWarnings("synthetic-access")
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (SREConfigurationBlock.this.systemSREButton.getSelection()) {
							handleSystemWideConfigurationSelected();
						}
					}
				});
			}
			
			if (this.projectProvider != null) {
				this.projectSREButton = SWTFactory.createRadioButton(group,
						MessageFormat.format(
								Messages.SREConfigurationBlock_3, Messages.SREConfigurationBlock_0), 3);
				this.projectSREButton.addSelectionListener(new SelectionAdapter() {
					@SuppressWarnings("synthetic-access")
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (SREConfigurationBlock.this.projectSREButton.getSelection()) {
							handleProjectConfigurationSelected();
						}
					}
				});
			}
			
			this.specificSREButton = SWTFactory.createRadioButton(group,
					Messages.SREConfigurationBlock_2, 1);
			this.specificSREButton.addSelectionListener(new SelectionAdapter() {
				@SuppressWarnings("synthetic-access")
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (SREConfigurationBlock.this.specificSREButton.getSelection()) {
						handleSpecificConfigurationSelected();
					}
				}
			});
		} else {
			this.systemSREButton = null;
			this.projectSREButton = null;
			this.specificSREButton = null;
		}
		
		this.runtimeEnvironmentCombo = SWTFactory.createCombo(
				group,
				SWT.DROP_DOWN | SWT.READ_ONLY,
				1,
				new String[0]);
		this.runtimeEnvironmentCombo.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void widgetSelected(SelectionEvent e) {
				firePropertyChange();
			}
		});
		ControlAccessibleListener.addListener(this.runtimeEnvironmentCombo, group.getText());
		
		this.runtimeEnvironmentSearchButton = SWTFactory.createPushButton(
				group, Messages.RuntimeEnvironmentTab_2, null);
		this.runtimeEnvironmentSearchButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleInstalledSREsButtonSelected();
			}
		});
		
		return getControl();
	}
	
	/** Update the label of the "system-wide" and "project" configuration buttons.
	 */
	public void updateExternalSREButtonLabels() {
		if (this.enableSystemWideSelector) {
			ISREInstall wideSystemSRE = SARLRuntime.getDefaultSREInstall();
			String wideSystemSRELabel;
			if (wideSystemSRE == null) {
				wideSystemSRELabel = Messages.SREConfigurationBlock_0;
			} else {
				wideSystemSRELabel = wideSystemSRE.getName();
			}
			this.systemSREButton.setText(MessageFormat.format(
					Messages.SREConfigurationBlock_1, wideSystemSRELabel));
		}
		if (this.projectProvider != null) {
			ISREInstall projectSRE = retreiveProjectSRE();
			String projectSRELabel;
			if (projectSRE == null) {
				projectSRELabel = Messages.SREConfigurationBlock_0;
			} else {
				projectSRELabel = projectSRE.getName();
			}
			this.projectSREButton.setText(MessageFormat.format(
					Messages.SREConfigurationBlock_3, projectSRELabel));
		}
	}

	/**
	 * Returns this block's control.
	 *
	 * @return control
	 */
	public Composite getControl() {
		return this.control;
	}

	/** Change the selected SRE. If <code>null</code> is given as parameter,
	 * and this block enables to select the system-wide SRE, then
	 * the system-wide SRE is selected. If the block is not enabling the
	 * system-wide SRE, the specific SRE is reset to the system-wide
	 * SRE.
	 *
	 * @param sre - the sre, if <code>null</code> reset to default.
	 * @return <code>true</code> if the selection changed.
	 * @see #selectSpecificSRE(ISREInstall)
	 */
	public final boolean selectSRE(ISREInstall sre) {
		if (sre == null) {
			if (this.enableSystemWideSelector) {
				return selectSystemWideSRE();
			}
			if (this.projectProvider != null) {
				return selectProjectSRE();
			}
		}
		return selectSpecificSRE(sre);
	}

	/** Select the system-wide SRE.
	 *
	 * @return <code>true</code> if the selection changed.
	 */
	public boolean selectSystemWideSRE() {
		if (this.enableSystemWideSelector && !this.systemSREButton.getSelection()) {
			doSystemSREButtonClick();
			return true;
		}
		return false;
	}

	/** Select the project SRE.
	 *
	 * @return <code>true</code> if the selection changed.
	 */
	public boolean selectProjectSRE() {
		if (this.projectProvider != null && !this.projectSREButton.getSelection()) {
			doProjectSREButtonClick();
			return true;
		}
		return false;
	}

	/** Select the specific SRE.
	 *
	 * @return <code>true</code> if the selection changed.
	 */
	public boolean selectSpecificSRE() {
		if (this.enableSystemWideSelector && this.systemSREButton.getSelection()) {
			doSpecificSREButtonClick();
			return true;
		}
		return false;
	}

	/** Select a specific SRE.
	 *
	 * @param sre - the sre, if <code>null</code> reset to default.
	 * @return <code>true</code> if the selection changed.
	 */
	public boolean selectSpecificSRE(ISREInstall sre) {
		ISREInstall theSRE = sre;
		if (theSRE == null) {
			theSRE = SARLRuntime.getDefaultSREInstall();
		}
		if (theSRE != null) {
			boolean changed = false;
			boolean oldNotify = this.notify;
			try {
				this.notify = false;
				if (isSystemWideDefaultSRE()) {
					doSpecificSREButtonClick();
					changed = true;
				}
				ISREInstall oldSRE = getSelectedSRE();
				if (oldSRE != theSRE) {
					int index = indexOf(theSRE);
					if (index >= 0) {
						this.runtimeEnvironmentCombo.select(index);
						changed = true;
					}
				}
				if (!this.runtimeEnvironments.isEmpty()) {
					int selection = this.runtimeEnvironmentCombo.getSelectionIndex();
					if (selection < 0 || selection >= this.runtimeEnvironments.size()) {
						// Ensure that there is a selected element
						selection = indexOf(theSRE);
						this.runtimeEnvironmentCombo.select((selection < 0) ? 0 : selection);
						changed = true;
					}
				}
			} finally {
				this.notify = oldNotify;
			}
			if (changed) {
				firePropertyChange();
				return true;
			}
		}
		return false;
	}

	private int indexOf(ISREInstall sre) {
		Iterator<ISREInstall> iterator = this.runtimeEnvironments.iterator();
		for (int i = 0; iterator.hasNext(); ++i) {
			ISREInstall s = iterator.next();
			if  (s.getId().equals(sre.getId())) {
				return i;
			}
		}
		return -1;
	}

	private String[] getSRELabels() {
		String[] labels = new String[this.runtimeEnvironments.size()];
		for (int i = 0; i < this.runtimeEnvironments.size(); ++i) {
			labels[i] = this.runtimeEnvironments.get(i).getName();
		}
		return labels;
	}

	/** Replies if the user has selected the default system-wide SRE.
	 * 
	 * @return <code>true</code> if the user has selected the default
	 * system-wide SRE.
	 * @see #getSelectedSRE()
	 */
	public boolean isSystemWideDefaultSRE() {
		return this.enableSystemWideSelector && this.systemSREButton.getSelection();
	}

	/** Replies if the user has selected the project SRE.
	 * 
	 * @return <code>true</code> if the user has selected the project SRE.
	 * @see #getSelectedSRE()
	 */
	public boolean isProjectSRE() {
		return this.projectProvider != null && this.projectSREButton.getSelection();
	}

	/** Replies the selected SARL runtime environment.
	 *
	 * @return the SARL runtime environment or <code>null</code> if
	 * there is no selected SRE.
	 * @see #isSystemWideDefaultSRE()
	 */
	public ISREInstall getSelectedSRE() {
		if (this.enableSystemWideSelector && this.systemSREButton.getSelection()) {
			return SARLRuntime.getDefaultSREInstall();
		}
		if (this.projectProvider != null && this.projectSREButton.getSelection()) {
			return retreiveProjectSRE();
		}
		return getSpecificSRE();
	}

	/** Replies the specific SARL runtime environment.
	 *
	 * @return the SARL runtime environment or <code>null</code> if
	 * there is no selected SRE.
	 * @see #isSystemWideDefaultSRE()
	 */
	public ISREInstall getSpecificSRE() {
		int index = this.runtimeEnvironmentCombo.getSelectionIndex();
		if (index >= 0 && index < this.runtimeEnvironments.size()) {
			return this.runtimeEnvironments.get(index);
		}
		return null;
	}

	/** Update the enable state of the block.
	 */
	public void updateEnableState() {
		boolean comboEnabled = !this.runtimeEnvironments.isEmpty();
		boolean searchEnabled = true;
		if (isSystemWideDefaultSRE() || isProjectSRE()) {
			comboEnabled = false;
			searchEnabled = false;
		}
		this.runtimeEnvironmentCombo.setEnabled(comboEnabled);
		this.runtimeEnvironmentSearchButton.setEnabled(searchEnabled);
	}

	/** Initialize the block with the installed JREs.
	 * The selection and the components states are not updated
	 * by this function.
	 */
	public void initialize() {
		// Initialize the SRE list
		this.runtimeEnvironments.clear();
		ISREInstall[] sres = SARLRuntime.getSREInstalls();
		Arrays.sort(sres, new Comparator<ISREInstall>() {
			@Override
			public int compare(ISREInstall o1, ISREInstall o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		List<String> labels = new ArrayList<>(sres.length);
		for (int i = 0; i < sres.length; ++i) {
			if (sres[i].getValidity().isOK()) {
				this.runtimeEnvironments.add(sres[i]);
				labels.add(sres[i].getName());
			}
		}
		this.runtimeEnvironmentCombo.setItems(labels.toArray(new String[labels.size()]));
		
		// Reset the labels of the external SRE configurations
		updateExternalSREButtonLabels();
		
		// Initialize the type of configuration
		if (this.enableSystemWideSelector) {
			this.specificSREButton.setSelection(false);
			if (this.projectProvider != null) {
				this.projectSREButton.setSelection(false);
			}
			this.systemSREButton.setSelection(true);
		} else if (this.projectProvider != null) {
			this.specificSREButton.setSelection(false);
			if (this.enableSystemWideSelector) {
				this.systemSREButton.setSelection(false);
			}
			this.projectSREButton.setSelection(true);
		}
		
		// Wait for SRE list updates.
		this.sreListener = new InstallChange();
		SARLRuntime.addSREInstallChangedListener(this.sreListener);
		
		updateEnableState();
	}
	
	/** Dispose the block.
	 */
	public void dispose() {
		if (this.sreListener != null) {
			SARLRuntime.removeSREInstallChangedListener(this.sreListener);
			this.sreListener = null;
		}
	}

	/** Invoked when the user want to search for a SARL runtime environment.
	 */
	protected void handleInstalledSREsButtonSelected() {
		PreferencesUtil.createPreferenceDialogOn(
				getControl().getShell(),
				SREsPreferencePage.ID,
				new String[] {SREsPreferencePage.ID},
				null).open();
	}

	/** Invoked when the user select the "system-wide" configuration.
	 */
	protected void handleSystemWideConfigurationSelected() {
		updateEnableState();
		firePropertyChange();
	}

	/** Invoked when the user select the "project" configuration.
	 */
	protected void handleProjectConfigurationSelected() {
		updateEnableState();
		firePropertyChange();
	}

	/** Invoked when the user select the "specific" configuration.
	 */
	protected void handleSpecificConfigurationSelected() {
		updateEnableState();
		firePropertyChange();
	}

	/** Validate that the given SRE is valid in the context of the SRE configuration.
	 *
	 * @param sre - the SRE.
	 * @return the state of the validation, never <code>null</code>.
	 */
	public IStatus validate(ISREInstall sre) {
		if (this.enableSystemWideSelector && this.systemSREButton.getSelection()) {
			if (SARLRuntime.getDefaultSREInstall() == null) {
				return PluginUtil.createStatus(IStatus.ERROR, Messages.SREConfigurationBlock_5);
			}
			return PluginUtil.createOkStatus();
		}
		if (this.projectProvider != null && this.projectSREButton.getSelection()) {
			if (null == null) {
				return PluginUtil.createStatus(IStatus.ERROR,
						Messages.SREConfigurationBlock_6);
			}
			return PluginUtil.createOkStatus();
		}
		if (this.runtimeEnvironments.isEmpty()) {
			return PluginUtil.createStatus(IStatus.ERROR,
					Messages.RuntimeEnvironmentTab_7);
		}
		return sre.getValidity();
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private class InstallChange implements ISREInstallChangedListener {

		/**
		 */
		public InstallChange() {
			//
		}

		@SuppressWarnings("synthetic-access")
		@Override
		public void sreRemoved(ISREInstall sre) {
			int index = indexOf(sre);
			if (index >= 0) {
				SREConfigurationBlock.this.runtimeEnvironments.remove(index);
				SREConfigurationBlock.this.runtimeEnvironmentCombo.setItems(getSRELabels());
				index = SREConfigurationBlock.this.runtimeEnvironmentCombo.getSelectionIndex();
				if (index < 0) {
					index = SREConfigurationBlock.this.runtimeEnvironmentCombo.getItemCount() - 1;
					SREConfigurationBlock.this.runtimeEnvironmentCombo.select(index);
				}
			}
		}

		@SuppressWarnings("synthetic-access")
		@Override
		public void sreChanged(org.eclipse.jdt.launching.PropertyChangeEvent event) {
			if (PROPERTY_NAME.equals(event.getProperty())) {
				ISREInstall sre = (ISREInstall) event.getSource();
				if (indexOf(sre) >= 0) {
					SREConfigurationBlock.this.runtimeEnvironmentCombo.setItems(getSRELabels());
					int index = SREConfigurationBlock.this.runtimeEnvironmentCombo.getSelectionIndex();
					if (index < 0) {
						index = indexOf(sre);
						if (index < 0) {
							index = SREConfigurationBlock.this.runtimeEnvironmentCombo.getItemCount() - 1;
						}
						SREConfigurationBlock.this.runtimeEnvironmentCombo.select(index);
					}
					if (sre == SARLRuntime.getDefaultSREInstall()) {
						updateExternalSREButtonLabels();
					}
				}
			}
		}

		@SuppressWarnings("synthetic-access")
		@Override
		public void sreAdded(ISREInstall sre) {
			if (validate(sre).isOK()) {
				ISREInstall current = getSpecificSRE();
				SREConfigurationBlock.this.runtimeEnvironments.add(sre);
				SREConfigurationBlock.this.runtimeEnvironmentCombo.setItems(getSRELabels());
				int index = indexOf(current);
				if (index >= 0) {
					SREConfigurationBlock.this.runtimeEnvironmentCombo.select(index);
				}
			}
		}

		/** {@inheritDoc}
		 */
		@Override
		public void defaultSREInstallChanged(ISREInstall previous, ISREInstall current) {
			updateExternalSREButtonLabels();
		}

	}

	/** The objects that are implementing this interface are able to
	 * provide a project to the {@link SREConfigurationBlock}.
	 * This project is used for managing the SRE configuration.
	 * 
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	public static interface SRECompliantProjectProvider {
		
		/** Replies the project that may provide a SRE configuration.
		 * 
		 * @return the project.
		 */
		IProject getSRECompliantProject();
		
	}
	
}
