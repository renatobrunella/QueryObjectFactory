/*
 * Copyright 2008 brunella ltd
 *
 * Licensed under the LGPL Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package sf.qof.plugin.wizards.addsupport;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import sf.qof.plugin.common.dialogs.FolderSelectionDialog;

public class AddSupportSettings extends WizardPage {

  private Button browseLibFolderButton;
  private Button importRequiredLibraryButton;
  private Text textLibraryFolder;
  private IProject project;
  
  /**
   * Create the wizard
   */
  public AddSupportSettings(IProject project) {
    super("settings");
    setTitle("Settings and options");
    setDescription("Add libraries to project and specify default code generation options");
    this.project = project;
  }

  /**
   * Create contents of the wizard
   * @param parent
   */
  public void createControl(Composite parent) {
    Composite container = new Composite(parent, SWT.NULL);
    container.setLayout(new GridLayout());
    setControl(container);

    Group importLibraryGroup;
    Label libraryFolderLabel;

    importLibraryGroup = new Group(container, SWT.NONE);
    importLibraryGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    final GridLayout gridLayout_2 = new GridLayout();
    gridLayout_2.numColumns = 3;
    importLibraryGroup.setLayout(gridLayout_2);
    importLibraryGroup.setText("Import Library");
    importRequiredLibraryButton = new Button(importLibraryGroup, SWT.CHECK);
    importRequiredLibraryButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(final SelectionEvent e) {
        if (importRequiredLibraryButton.getSelection()) {
          textLibraryFolder.setEnabled(true);
          browseLibFolderButton.setEnabled(true);
        } else {
          textLibraryFolder.setEnabled(false);
          browseLibFolderButton.setEnabled(false);
        }
      }
    });
    importRequiredLibraryButton.setSelection(true);
    importRequiredLibraryButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
    importRequiredLibraryButton.setText("Import required library to project");
    libraryFolderLabel = new Label(importLibraryGroup, SWT.NONE);
    libraryFolderLabel.setLayoutData(new GridData(191, SWT.DEFAULT));
    libraryFolderLabel.setText("Library folder:");

    textLibraryFolder = new Text(importLibraryGroup, SWT.BORDER);
    final GridData gd_textLibraryFolder = new GridData(SWT.FILL, SWT.CENTER, true, false);
    textLibraryFolder.setLayoutData(gd_textLibraryFolder);
    browseLibFolderButton = new Button(importLibraryGroup, SWT.NONE);
    browseLibFolderButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(final SelectionEvent e) {
        FolderSelectionDialog dialog = new FolderSelectionDialog(getShell(), project, 
            new Class<?>[] { IProject.class, IFolder.class }, null, null);
        dialog.setTitle("Choose Folder");
        dialog.setMessage("Select folder for the library files");
        String folderName = textLibraryFolder.getText().trim();
        if (!folderName.equals("")) {
          IFolder initialFolder = project.getFolder(folderName);
          if (initialFolder.exists()) {
            dialog.setInitialSelection(initialFolder);
          }
        }
        if (dialog.open() == Window.OK) {
          textLibraryFolder.setText(dialog.getSelectedContainer().getProjectRelativePath().toString());
        }
      }
    });
    final GridData gd_browseLibFolderButton = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
    gd_browseLibFolderButton.widthHint = 70;
    browseLibFolderButton.setLayoutData(gd_browseLibFolderButton);
    browseLibFolderButton.setText("Browse...");
  }

  public String getLibraryFolder() {
    return textLibraryFolder.getText();
  }

  public boolean getImportRequiredLibrary() {
    return importRequiredLibraryButton.getSelection();
  }
}
