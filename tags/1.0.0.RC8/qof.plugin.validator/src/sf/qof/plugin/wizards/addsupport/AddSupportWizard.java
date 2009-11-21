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

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.Wizard;

import sf.qof.plugin.utils.ProjectHelper;

public class AddSupportWizard extends Wizard {

  private IProject project;
  private AddSupportSettings addSupportSettingsPage;

  public AddSupportWizard(IProject project) {
    this.project = project;
    setWindowTitle("Add QueryObjectFactory support");
    
    addSupportSettingsPage = new AddSupportSettings(project);
    
    addPage(addSupportSettingsPage);
  }
  
  @Override
  public boolean performFinish() {
    if (addSupportSettingsPage.getImportRequiredLibrary()) {
      ProjectHelper.addQOFLibraryToProject(project, addSupportSettingsPage.getLibraryFolder());
    }
    return true;
  }

}
