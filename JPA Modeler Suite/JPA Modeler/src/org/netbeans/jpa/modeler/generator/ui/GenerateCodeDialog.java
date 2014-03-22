/**
 * Copyright [2014] Gaurav Gupta
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.netbeans.jpa.modeler.generator.ui;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.jpa.modeler.core.widget.ui.GenericDialog;
import org.netbeans.modules.profiler.api.ProjectUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Gaurav_Gupta
 */
public class GenerateCodeDialog extends GenericDialog
        implements PropertyChangeListener {

    private FileObject modelerFileObject;

    /**
     * Creates new form GenerateCodeDialog
     */
    public GenerateCodeDialog(FileObject fileObject) {
        this.modelerFileObject = fileObject;
        initComponents();
        propertyChangeSupport = new PropertyChangeSupport(this);

        populateExistingProjectElementGroup();
        backupSourcesCheck.setSelected(true);
        getPropertyChangeSupport().addPropertyChangeListener(this);
        this.setTitle("Generate Source Code");
        backupSourcesCheck.setVisible(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLayeredPane1 = new javax.swing.JLayeredPane();
        backupSourcesCheck = new javax.swing.JCheckBox();
        targetProjectCombo = new javax.swing.JComboBox();
        srcFolderCombo = new javax.swing.JComboBox();
        targetProject = new javax.swing.JLabel();
        srcFolder = new javax.swing.JLabel();
        jLayeredPane2 = new javax.swing.JLayeredPane();
        generateSourceCode = new javax.swing.JButton();
        cencelGenerateCode = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        backupSourcesCheck.setSelected(true);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/jpa/modeler/generator/ui/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(backupSourcesCheck, bundle.getString("GenerateCodeDialog.backupSourcesCheck.text")); // NOI18N
        backupSourcesCheck.setMargin(new java.awt.Insets(0, 0, 0, 0));

        targetProjectCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                targetProjectComboItemStateChanged(evt);
            }
        });

        srcFolderCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                srcFolderComboItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(targetProject, org.openide.util.NbBundle.getMessage(GenerateCodeDialog.class, "GenerateCodeDialog.targetProject.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(srcFolder, org.openide.util.NbBundle.getMessage(GenerateCodeDialog.class, "GenerateCodeDialog.srcFolder.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(generateSourceCode, org.openide.util.NbBundle.getMessage(GenerateCodeDialog.class, "GenerateCodeDialog.generateSourceCode.text")); // NOI18N
        generateSourceCode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateSourceCodeActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cencelGenerateCode, org.openide.util.NbBundle.getMessage(GenerateCodeDialog.class, "GenerateCodeDialog.cencelGenerateCode.text")); // NOI18N
        cencelGenerateCode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cencelGenerateCodeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jLayeredPane2Layout = new javax.swing.GroupLayout(jLayeredPane2);
        jLayeredPane2.setLayout(jLayeredPane2Layout);
        jLayeredPane2Layout.setHorizontalGroup(
            jLayeredPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(generateSourceCode)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cencelGenerateCode))
        );
        jLayeredPane2Layout.setVerticalGroup(
            jLayeredPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane2Layout.createSequentialGroup()
                .addGroup(jLayeredPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(generateSourceCode)
                    .addComponent(cencelGenerateCode))
                .addGap(0, 3, Short.MAX_VALUE))
        );
        jLayeredPane2.setLayer(generateSourceCode, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane2.setLayer(cencelGenerateCode, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jLayeredPane1Layout = new javax.swing.GroupLayout(jLayeredPane1);
        jLayeredPane1.setLayout(jLayeredPane1Layout);
        jLayeredPane1Layout.setHorizontalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane1Layout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jLayeredPane1Layout.createSequentialGroup()
                        .addComponent(backupSourcesCheck, javax.swing.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE)
                        .addGap(67, 67, 67))
                    .addGroup(jLayeredPane1Layout.createSequentialGroup()
                        .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jLayeredPane1Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jLayeredPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jLayeredPane1Layout.createSequentialGroup()
                                .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(srcFolder, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(targetProject, javax.swing.GroupLayout.Alignment.LEADING))
                                .addGap(33, 33, 33)
                                .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(srcFolderCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(targetProjectCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addGap(44, 44, 44))))
        );
        jLayeredPane1Layout.setVerticalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane1Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(targetProject)
                    .addComponent(targetProjectCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(srcFolder)
                    .addComponent(srcFolderCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(backupSourcesCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLayeredPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
        );
        jLayeredPane1.setLayer(backupSourcesCheck, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(targetProjectCombo, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(srcFolderCombo, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(targetProject, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(srcFolder, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(jLayeredPane2, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLayeredPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLayeredPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
   public final static String PROP_TARGET_PROJECT = "TARGET_PROJECT"; // NOI18N
    public final static String PROP_NO_TARGET_PROJECT = "NO_TARGET_PROJECT"; // NOI18N
    public final static String PROP_SOURCE_FOLDER = "SOURCE_FOLDER"; // NOI18N
    public final static String PROP_NO_SOURCE_FOLDER = "NO_SOURCE_FOLDER"; // NOI18N

    private Project targetPrj = null;
    private Project origPrj = null;
    private SourceGroup sourceGroup = null;
    private boolean noTargetProject = false;
    private boolean noOpenTargets = false;

    private void targetProjectComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_targetProjectComboItemStateChanged
        // TODO add your handling code here:
        targetPrj = (Project) targetProjectCombo.getSelectedItem();
        populateSourceFolderCombo(targetPrj);

        String prop = targetPrj == null
                ? PROP_NO_TARGET_PROJECT : PROP_TARGET_PROJECT;

        getPropertyChangeSupport().firePropertyChange(prop, null, evt);
    }//GEN-LAST:event_targetProjectComboItemStateChanged

    private void srcFolderComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_srcFolderComboItemStateChanged
        // TODO add your handling code here:
        setSourceGroup((SourceGroup) srcFolderCombo.getSelectedItem());
    }//GEN-LAST:event_srcFolderComboItemStateChanged

    private void generateSourceCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateSourceCodeActionPerformed
        if (sourceGroup == null) {
            NotifyDescriptor d = new NotifyDescriptor.Message("Please select the Source Folder .", NotifyDescriptor.INFORMATION_MESSAGE);
            d.setTitle("Source Folder");
            DialogDisplayer.getDefault().notify(d);
        } else {
            setVisible(false);
            this.setDialogResult(javax.swing.JOptionPane.OK_OPTION);
            dispose();
        }
//        srcGroup.getRootFolder().getPath()
    }//GEN-LAST:event_generateSourceCodeActionPerformed

    private void cencelGenerateCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cencelGenerateCodeActionPerformed
        cancelActionPerformed(evt);
    }//GEN-LAST:event_cencelGenerateCodeActionPerformed

    public String getSelectedFolderName() {
        try {
            if (getSourceGroup() != null && getSourceGroup().getRootFolder() != null) {
                return FileUtil.toFile(getSourceGroup().getRootFolder()).getCanonicalPath();
            }
        } catch (IOException e) {
        }

        return null;
    }

    public boolean isBackupSources() {
        return backupSourcesCheck != null ? backupSourcesCheck.isSelected() : true;
    }

    public void propertyChange(PropertyChangeEvent event) {
        String propName = "";

        if (event != null) {
            propName = event.getPropertyName();
        }

//        if (propName.equals(TemplateModel.PROP_NO_TEMPLATES_ENABLED)) {
//            noTemplatesEnabled = true;
//        } else if (propName.equals(TemplateModel.PROP_ONE_TEMPLATE_ENABLED)) {
//            noTemplatesEnabled = false;
//        } else
        if (propName.equals(PROP_TARGET_PROJECT)) {
            noTargetProject = false;
        } else if (propName.equals(PROP_NO_TARGET_PROJECT)) {
            noTargetProject = true;
        }

        String msg = "";

        if (noOpenTargets) {
            msg = NbBundle.getMessage(
                    GenerateCodeDialog.class, "MSG_NoOpenTargets"); // NIO18N
        } else if (noTargetProject) {
            msg = NbBundle.getMessage(
                    GenerateCodeDialog.class, "MSG_NoTargetJavaProject"); // NIO18N
        }

//        statusLabel.setText(msg);
//
//        statusLabel.setVisible(
//                noTargetProject || noOpenTargets || noTemplatesEnabled);
//
//        firePropertyChange(GenerateCodeDescriptor.PROP_VALID, null,
//                !(noTargetProject || noOpenTargets || noTemplatesEnabled));
    }

    //
    // target project added elements processing
    //
    private void enableExistingProjectElementGroup(boolean enable) {
        targetProjectCombo.setEnabled(enable);
        srcFolderCombo.setEnabled(enable);
    }

    private void populateExistingProjectElementGroup() {
        GenerateCodeDialog.ProjectCellRenderer projectCellRenderer = new GenerateCodeDialog.ProjectCellRenderer(targetProjectCombo.getRenderer());
        targetProjectCombo.setRenderer(projectCellRenderer);
        Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
        ArrayList<Project> list = new ArrayList<Project>();

        if (openProjects != null) {
            for (Project prj : openProjects) {
                Sources sources = ProjectUtils.getSources(prj);
                if (sources == null) {
                    continue;
                }

                SourceGroup[] srcGrps = sources.getSourceGroups(
                        JavaProjectConstants.SOURCES_TYPE_JAVA);

                if (srcGrps != null && srcGrps.length > 0) {
                    list.add(prj);
                }
            }
        }

        if (origPrj != null && !list.contains(origPrj)) {
            list.add(origPrj);
        }

        if (list == null || list.size() == 0) {
            noOpenTargets = true;
            enableExistingProjectElementGroup(false);
        } else {
            //list.add(0, null);

            DefaultComboBoxModel projectsModel
                    = new DefaultComboBoxModel(list.toArray());

            targetProjectCombo.setModel(projectsModel);

            // Issue Fix #5850 Start
            Project project = FileOwnerQuery.getOwner(modelerFileObject);
            if (project != null) {
                targetProjectCombo.setSelectedItem(project);
            } else {
                targetProjectCombo.setSelectedIndex(-1);
            }
             // Issue Fix #5850 End

            // When the selected index was set to -1 it reset the targetPrj
            // value.  Since the targetPrj was simply initialized with the
            // origPrj value, just set it again.
            targetPrj = origPrj;
            selectTargetProject();
            noOpenTargets = false;
            // enableExistingProjectElementGroup(true);
        }

        propertyChange(null);
    }

    private void selectTargetProject() {
        if (targetPrj == null) {
            if (targetProjectCombo.getSelectedItem() != null) {
                targetPrj = (Project) targetProjectCombo.getSelectedItem();
                srcFolderCombo.setEnabled(true);
            } else {
                srcFolderCombo.setEnabled(false);
            }
        } else {
            targetProjectCombo.setSelectedItem(targetPrj);
            srcFolderCombo.setEnabled(true);
        }

        if (targetProjectCombo.getSelectedItem() != null) {
            Project prj = (Project) targetProjectCombo.getSelectedItem();
            populateSourceFolderCombo(prj);
            srcFolderCombo.setEnabled(true);
        }
    }

    private void populateSourceFolderCombo(Project prj) {
        GenerateCodeDialog.SourceRootCellRenderer srcCellRenderer
                = new GenerateCodeDialog.SourceRootCellRenderer(srcFolderCombo.getRenderer());
        srcFolderCombo.setRenderer(srcCellRenderer);
        ArrayList<SourceGroup> srcRoots = new ArrayList<SourceGroup>();
        int index = 0;
        FileObject sfo = null;

        if (getSourceGroup() != null) {
            sfo = getSourceGroup().getRootFolder();
        }

        if (prj != null) {
            Sources sources = ProjectUtils.getSources(prj);
            if (sources != null) {
                SourceGroup[] srcGrps = sources.getSourceGroups(
                        JavaProjectConstants.SOURCES_TYPE_JAVA);

                if (srcGrps != null) {
                    for (SourceGroup g : srcGrps) {
                        if (g != null) {
                            srcRoots.add(g);

                            if (g.getRootFolder() != null
                                    && g.getRootFolder().equals(sfo)) {
                                index = srcRoots.size() - 1;
                            }
                        }
                    }
                }
            }
        }

        DefaultComboBoxModel rootsModel
                = new DefaultComboBoxModel(srcRoots.toArray());

        srcFolderCombo.setModel(rootsModel);

        if (srcRoots.size() > 0) {
            srcFolderCombo.setSelectedIndex(index);
            setSourceGroup(srcRoots.get(index));
            srcFolderCombo.setEnabled(true);
        } else {
            srcFolderCombo.setEnabled(false);
        }
    }

    private boolean setTargetElementsFromFolder(String path) {
        if (path == null) {
            return false;
        }

        FileObject fo = null;

        try {
            File normalizedFile = FileUtil.normalizeFile(new File(path));
            fo = FileUtil.toFileObject(new File(normalizedFile.getCanonicalPath()));
        } catch (IOException ex) {
            return false;
        } catch (Exception e) {
            return false;
        }
        if (fo == null) {
            return false;
        }

        Project prj = FileOwnerQuery.getOwner(fo);
        if (prj == null) {
            return false;
        }

        Sources sources = ProjectUtils.getSources(prj);
        if (sources == null) {
            return false;
        }

        SourceGroup[] srcGrps = sources.getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);

        if (srcGrps != null && srcGrps.length > 0) {
            for (SourceGroup g : srcGrps) {
                FileObject root = g.getRootFolder();

                if (!fo.equals(root)) {
                    continue;
                } else {
                    origPrj = prj;
                    setSourceGroup(g);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * @return the sourceGroup
     */
    public SourceGroup getSourceGroup() {
        return sourceGroup;
    }

    /**
     * @param sourceGroup the sourceGroup to set
     */
    public void setSourceGroup(SourceGroup sourceGroup) {
        this.sourceGroup = sourceGroup;
    }

    /**
     * @return the modelerFileObject
     */
    public FileObject getModelerFileObject() {
        return modelerFileObject;
    }

    /**
     * @param modelerFileObject the modelerFileObject to set
     */
    public void setModelerFileObject(FileObject modelerFileObject) {
        this.modelerFileObject = modelerFileObject;
    }

    /**
     * copy from ReverseEngineerPanel.java
     */
    private static class ProjectCellRenderer extends JLabel
            implements ListCellRenderer {

        ListCellRenderer renderer;

        public ProjectCellRenderer(ListCellRenderer hostRenderer) {
            renderer = hostRenderer;
        }

        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            Component comp = null;

            if (renderer != null) {
                comp = renderer.getListCellRendererComponent(list, value, index,
                        isSelected, cellHasFocus);
            }

            JLabel label = null;

            if (comp instanceof JLabel) {
                label = (JLabel) comp;
            } else {
                label = this;
                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                } else {
                    setBackground(list.getBackground());
                    setForeground(list.getForeground());
                }
                setOpaque(true);
            }

            if (value instanceof Project) {
                ProjectInformation pi = ProjectUtils.getInformation((Project) value);

                label.setText(pi.getDisplayName());
                label.setIcon(pi.getIcon());
            } else {
                label.setText(value == null ? " " : value.toString()); // NOI18N
                label.setIcon(null);
            }
            return label;
        }
    }

    private static class SourceRootCellRenderer extends JLabel
            implements ListCellRenderer {

        ListCellRenderer renderer;

        public SourceRootCellRenderer(ListCellRenderer hostRenderer) {
            renderer = hostRenderer;
        }

        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            Component comp = null;

            if (renderer != null) {
                comp = renderer.getListCellRendererComponent(list, value, index,
                        isSelected, cellHasFocus);
            }

            JLabel label = null;

            if (comp instanceof JLabel) {
                label = (JLabel) comp;
            } else {
                label = this;
                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                } else {
                    setBackground(list.getBackground());
                    setForeground(list.getForeground());
                }
                setOpaque(true);
            }

            if (value instanceof SourceGroup) {
                SourceGroup sg = (SourceGroup) value;
                String desc = sg.getDisplayName();
                if (desc == null || desc.length() == 0) {
                    FileObject fo = sg.getRootFolder();
                    desc = fo.getPath();
                }
                label.setText(desc);
            } else {
                label.setText(value == null ? " " : value.toString()); // NOI18N
            }
            return label;
        }
    }

    private PropertyChangeSupport propertyChangeSupport = null;

    public PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox backupSourcesCheck;
    private javax.swing.JButton cencelGenerateCode;
    private javax.swing.JButton generateSourceCode;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JLayeredPane jLayeredPane2;
    private javax.swing.JLabel srcFolder;
    private javax.swing.JComboBox srcFolderCombo;
    private javax.swing.JLabel targetProject;
    private javax.swing.JComboBox targetProjectCombo;
    // End of variables declaration//GEN-END:variables

}

class JavaProjectConstants {

    private JavaProjectConstants() {
    }

    /**
     * Java package root sources type.
     *
     * @see org.netbeans.api.project.Sources
     */
    public static final String SOURCES_TYPE_JAVA = "java"; // NOI18N

    /**
     * Package root sources type for resources, if these are not put together
     * with Java sources.
     *
     * @see org.netbeans.api.project.Sources
     * @since org.netbeans.modules.java.project/1 1.11
     */
    public static final String SOURCES_TYPE_RESOURCES = "resources"; // NOI18N

    /**
     * Hint for <code>SourceGroupModifier</code> to create a
     * <code>SourceGroup</code> for main project codebase.
     *
     * @see org.netbeans.api.project.SourceGroupModifier
     * @since org.netbeans.modules.java.project/1 1.24
     */
    public static final String SOURCES_HINT_MAIN = "main"; //NOI18N

    /**
     * Hint for <code>SourceGroupModifier</code> to create a
     * <code>SourceGroup</code> for project's tests.
     *
     * @see org.netbeans.api.project.SourceGroupModifier
     * @since org.netbeans.modules.java.project/1 1.24
     */
    public static final String SOURCES_HINT_TEST = "test"; //NOI18N

    /**
     * Standard artifact type representing a JAR file, presumably used as a Java
     * library of some kind.
     *
     * @see org.netbeans.api.project.ant.AntArtifact
     */
    public static final String ARTIFACT_TYPE_JAR = "jar"; // NOI18N

    /**
     * Standard artifact type representing a folder containing classes,
     * presumably used as a Java library of some kind.
     *
     * @see org.netbeans.api.project.ant.AntArtifact
     * @since org.netbeans.modules.java.project/1 1.4
     */
    public static final String ARTIFACT_TYPE_FOLDER = "folder"; //NOI18N

    /**
     * Standard command for running Javadoc on a project.
     *
     * @see org.netbeans.spi.project.ActionProvider
     */
    public static final String COMMAND_JAVADOC = "javadoc"; // NOI18N

    /**
     * Standard command for reloading a class in a foreign VM and continuing
     * debugging.
     *
     * @see org.netbeans.spi.project.ActionProvider
     */
    public static final String COMMAND_DEBUG_FIX = "debug.fix"; // NOI18N

}