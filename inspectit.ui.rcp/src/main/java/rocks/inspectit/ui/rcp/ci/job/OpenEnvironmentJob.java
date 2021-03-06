package rocks.inspectit.ui.rcp.ci.job;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.ci.form.editor.EnvironmentEditor;
import rocks.inspectit.ui.rcp.ci.form.input.EnvironmentEditorInput;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

/**
 * Job for loading of the environment from the CMR and opening the environment editor.
 *
 * @author Ivan Senic
 *
 */
public class OpenEnvironmentJob extends Job {

	/**
	 * {@link CmrRepositoryDefinition}.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Environment id.
	 */
	private String environmentId;

	/**
	 * Page to open editor in.
	 */
	private IWorkbenchPage page;

	/**
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}
	 * @param environmentId
	 *            Profile id.
	 * @param page
	 *            Page to open editor in.
	 */
	public OpenEnvironmentJob(CmrRepositoryDefinition cmrRepositoryDefinition, String environmentId, IWorkbenchPage page) {
		super("Loading environment..");
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		this.environmentId = environmentId;
		this.page = page;
		setUser(true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if (cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.OFFLINE) {
			return Status.CANCEL_STATUS;
		}

		try {
			Environment environment = cmrRepositoryDefinition.getConfigurationInterfaceService().getEnvironment(environmentId);
			Collection<Profile> profiles = cmrRepositoryDefinition.getConfigurationInterfaceService().getAllProfiles();
			final EnvironmentEditorInput environmentEditorInput = new EnvironmentEditorInput(environment, profiles, cmrRepositoryDefinition);
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					try {
						page.openEditor(environmentEditorInput, EnvironmentEditor.ID, true);
					} catch (PartInitException e) {
						InspectIT.getDefault().createErrorDialog("Exception occurred opening the Environment editor.", e, -1);
					}
				}
			});
			return Status.OK_STATUS;
		} catch (BusinessException e) {
			return new Status(IStatus.ERROR, InspectIT.ID, "Exception occurred loading the environment from the CMR.", e);
		}
	}

}