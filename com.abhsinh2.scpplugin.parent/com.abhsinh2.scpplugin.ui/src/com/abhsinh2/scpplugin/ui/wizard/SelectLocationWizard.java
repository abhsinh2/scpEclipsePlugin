package com.abhsinh2.scpplugin.ui.wizard;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.abhsinh2.scpplugin.ui.Activator;
import com.abhsinh2.scpplugin.ui.model.SCPLocation;
import com.abhsinh2.scpplugin.ui.model.SCPLocationManager;
import com.abhsinh2.scpplugin.ui.model.remote.SCPRemoteLocation;
import com.abhsinh2.scpplugin.ui.util.SCPCopyLocalToRemote;

public class SelectLocationWizard extends Wizard implements INewWizard {

	private IStructuredSelection initialSelection;
	private SelectLocationWizardPage selectLocationWizardPage;
	private EnterNewLocationWizardPage enterNewLocationWizardPage;
	private SelectLocalFilesWizardPage selectLocalFilesWizardPage;
	private ExecutionEvent executionEvent;

	private SCPLocationManager locationManager = SCPLocationManager
			.getManager();

	private boolean enableFinishButton = false;

	public SelectLocationWizard(ExecutionEvent executionEvent) {
		this.executionEvent = executionEvent;

		IDialogSettings favoritesSettings = Activator.getDefault()
				.getDialogSettings();
		IDialogSettings wizardSettings = favoritesSettings
				.getSection("SelectLocationWizard");
		if (wizardSettings == null)
			wizardSettings = favoritesSettings
					.addNewSection("SelectLocationWizard");
		setDialogSettings(favoritesSettings);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.initialSelection = selection;
	}

	public void addPages() {
		setWindowTitle("SCP");

		if (locationManager.getAllLocations().isEmpty()) {
			enterNewLocationWizardPage = new EnterNewLocationWizardPage();
			addPage(enterNewLocationWizardPage);
		} else {
			selectLocationWizardPage = new SelectLocationWizardPage(
					this.initialSelection);
			addPage(selectLocationWizardPage);

			enterNewLocationWizardPage = new EnterNewLocationWizardPage();
			addPage(enterNewLocationWizardPage);
		}

		selectLocalFilesWizardPage = new SelectLocalFilesWizardPage(
				this.initialSelection);
		addPage(selectLocalFilesWizardPage);
	}

	@Override
	public boolean needsPreviousAndNextButtons() {
		return true;
	}

	@Override
	public boolean canFinish() {
		if (selectLocationWizardPage != null) {
			if (!selectLocationWizardPage.isAddNewLocation()
					&& selectLocationWizardPage.getLocation() != null) {
				return true;
			}
		}
		return enableFinishButton;
	}

	@Override
	public boolean performFinish() {
		SCPRemoteLocation remoteLocation = null;
		Collection<String> localFiles = null;

		if (selectLocationWizardPage == null) {
			remoteLocation = enterNewLocationWizardPage.getSCPRemoteLocation();
			localFiles = selectLocalFilesWizardPage.getLocalFilesPath();

			SCPLocation location = new SCPLocation(
					enterNewLocationWizardPage.getLocationName(), localFiles,
					remoteLocation);
			locationManager.addLocation(location);
		} else {
			if (selectLocationWizardPage.isAddNewLocation()) {
				remoteLocation = enterNewLocationWizardPage
						.getSCPRemoteLocation();

				if (selectLocationWizardPage.isUseSavedLocalFiles()) {
					localFiles = selectLocalFilesWizardPage.getLocalFilesPath();
				} else {
					localFiles = selectLocationWizardPage
							.getSelectedLocalFiles();
				}
			} else {
				SCPLocation location = selectLocationWizardPage.getLocation();
				remoteLocation = location.getRemoteLocation();

				if (selectLocationWizardPage.isUseSavedLocalFiles()) {
					localFiles = location.getLocalFiles();
				} else {
					localFiles = selectLocationWizardPage
							.getSelectedLocalFiles();
				}
			}
		}

		this.startCopying(remoteLocation, localFiles);

		return true;
	}

	private boolean startCopying(final SCPRemoteLocation remoteLocation,
			final Collection<String> localFiles) {
		try {
			getContainer().run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					performOperation(remoteLocation, localFiles, monitor);
				}
			});
		} catch (InvocationTargetException e) {
			System.out.println(e);
			return false;
		} catch (InterruptedException e) {
			return false;
		}

		return true;
	}

	private void performOperation(final SCPRemoteLocation remoteLocation,
			final Collection<String> localFiles, IProgressMonitor monitor) {
		System.out.println("remoteLocation:" + remoteLocation);
		System.out.println("localFiles:" + localFiles);
		try {
			monitor.beginTask("Preparing", localFiles.size());

			for (String localLocation : localFiles) {
				monitor.subTask("Copying " + localLocation);

				SCPCopyLocalToRemote copy = new SCPCopyLocalToRemote(
						localLocation, remoteLocation.getRemoteAddress(),
						remoteLocation.getRemoteLocation(),
						remoteLocation.getUsername(),
						remoteLocation.getPassword());

				// copy.copy();

				monitor.worked(1);

				if (monitor.isCanceled()) {
					break;
				}
			}

			monitor.done();
		} finally {
			// monitor.done();
		}
	}

	private void startCopyingInProgressDialog(
			final SCPRemoteLocation remoteLocation,
			final Collection<String> localFiles) {
		try {
			IWorkbenchWindow window = HandlerUtil
					.getActiveWorkbenchWindow(this.executionEvent);
			IRunnableContext context = window.getWorkbench()
					.getProgressService();
			context.run(true, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					performOperation(remoteLocation, localFiles, monitor);
				}
			});
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public boolean isEnableFinishButton() {
		return enableFinishButton;
	}

	public void setEnableFinishButton(boolean enableFinishButton) {
		this.enableFinishButton = enableFinishButton;
	}

}