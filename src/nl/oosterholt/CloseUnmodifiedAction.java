package nl.oosterholt;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@ActionID(
		category = "Editor",
		id = "nl.oosterholt.CloseUnmodifiedAction"
)
@ActionRegistration(
		displayName = "#CTL_CloseUnmodifiedAction"
)
@ActionReference(path = "Editors/TabActions", position = 0)
public final class CloseUnmodifiedAction implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent ev) {
		for (TopComponent tc : getUnchangedDocumentsForNB81()) {
			tc.close();
		}
	}

	/**
	 * Works SVN/HG/GIT since NB8.1. See
	 * https://netbeans.org/bugzilla/show_bug.cgi?id=248811
	 *
	 * @return
	 */
	public Collection<TopComponent> getUnchangedDocumentsForNB81() {
		final WindowManager wm = WindowManager.getDefault();
		final LinkedHashSet<TopComponent> result = new LinkedHashSet<TopComponent>();
		for (TopComponent tc : getCurrentEditors()) {
			if (!wm.isEditorTopComponent(tc)) {
				continue;
			}

			//check for the format of an unsaved file
			boolean isUnsaved = null != tc.getLookup().lookup(SaveCookie.class);
			if (isUnsaved) {
				continue;
			}

			DataObject dob = tc.getLookup().lookup(DataObject.class);
			if (dob != null) {
				final FileObject file = dob.getPrimaryFile();
				Object attribute = file.getAttribute("ProvidedExtensions.VCSIsModified");
				if (null != attribute) {
					if (Boolean.FALSE.equals(attribute)) {
						result.add(tc);
					}
				} else {
					//could not determine status, keep this document
				}
			} else {
				//close diff windows too
				result.add(tc);
			}
		}
		return result;
	}

	private Collection<TopComponent> getCurrentEditors() {
		final ArrayList<TopComponent> result = new ArrayList<TopComponent>();
		final WindowManager wm = WindowManager.getDefault();
		for (Mode mode : wm.getModes()) {
			if (wm.isEditorMode(mode)) {
				result.addAll(Arrays.asList(wm.getOpenedTopComponents(mode)));
			}
		}
		return result;
	}
}
