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
package sf.qof.plugin.common.dialogs;

import org.eclipse.core.resources.IContainer;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.dialogs.NewFolderDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;

public class FolderSelectionDialog extends ElementTreeSelectionDialog implements ISelectionChangedListener {

  private Button fNewFolderButton;
  private IContainer fSelectedContainer;

  public FolderSelectionDialog(Shell parent, Object root, Class<?>[] acceptedTypes, Object[] rejectedElements,
      ISelectionStatusValidator selectionValidator) {
    super(parent, new WorkbenchLabelProvider(), new WorkbenchContentProvider());
    setComparator(new ResourceComparator(ResourceComparator.NAME));

    ViewerFilter filter = new TypedViewerFilter(acceptedTypes, rejectedElements);
    addFilter(filter);
    setInput(root);
  }

  protected Control createDialogArea(Composite parent) {
    Composite result = (Composite) super.createDialogArea(parent);

    getTreeViewer().addSelectionChangedListener(this);

    Button button = new Button(result, SWT.PUSH);
    button.setText("Create New Folder...");
    button.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        newFolderButtonPressed();
      }
    });
    button.setFont(parent.getFont());
    fNewFolderButton = button;

    applyDialogFont(result);

    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
        IJavaHelpContextIds.BP_SELECT_DEFAULT_OUTPUT_FOLDER_DIALOG);

    return result;
  }

  private void updateNewFolderButtonState() {
    IStructuredSelection selection = (IStructuredSelection) getTreeViewer().getSelection();
    fSelectedContainer = null;
    if (selection.size() == 1) {
      Object first = selection.getFirstElement();
      if (first instanceof IContainer) {
        fSelectedContainer = (IContainer) first;
      }
    }
    fNewFolderButton.setEnabled(fSelectedContainer != null);
  }

  protected void newFolderButtonPressed() {
    NewFolderDialog dialog = new NewFolderDialog(getShell(), fSelectedContainer) {
      protected Control createContents(Composite parent) {
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IJavaHelpContextIds.BP_CREATE_NEW_FOLDER);
        return super.createContents(parent);
      }
    };
    if (dialog.open() == Window.OK) {
      TreeViewer treeViewer = getTreeViewer();
      treeViewer.refresh(fSelectedContainer);
      Object createdFolder = dialog.getResult()[0];
      treeViewer.reveal(createdFolder);
      treeViewer.setSelection(new StructuredSelection(createdFolder));
    }
  }

  public void selectionChanged(SelectionChangedEvent event) {
    updateNewFolderButtonState();
  }

  public IContainer getSelectedContainer() {
    return fSelectedContainer;
  }
}
