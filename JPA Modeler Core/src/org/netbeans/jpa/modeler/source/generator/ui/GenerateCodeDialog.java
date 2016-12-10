/**
 * Copyright [2016] Gaurav Gupta
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
package org.netbeans.jpa.modeler.source.generator.ui;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import static java.util.stream.Collectors.toList;
import javax.lang.model.SourceVersion;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.jcode.core.util.ProjectHelper;
import static org.netbeans.jcode.core.util.ProjectHelper.getJavaProjects;
import org.netbeans.jcode.core.util.ProjectType;
import static org.netbeans.jcode.core.util.SourceGroupSupport.getPackageForFolder;
import org.netbeans.jcode.layer.DefaultBusinessLayer;
import org.netbeans.jcode.layer.DefaultControllerLayer;
import org.netbeans.jcode.layer.DefaultViewerLayer;
import org.netbeans.jcode.layer.Generator;
import org.netbeans.jcode.layer.TechContext;
import static org.netbeans.jcode.layer.Technology.Type.BUSINESS;
import static org.netbeans.jcode.layer.Technology.Type.CONTROLLER;
import static org.netbeans.jcode.layer.Technology.Type.VIEWER;
import org.netbeans.jcode.stack.config.data.ApplicationConfigData;
import org.netbeans.jcode.stack.config.panel.LayerConfigPanel;
import org.netbeans.jcode.ui.source.ProjectCellRenderer;
import org.netbeans.jcode.ui.source.SourceRootCellRenderer;
import org.netbeans.jpa.modeler.spec.EntityMappings;
import org.netbeans.jpa.modeler.spec.extend.JavaClass;
import org.netbeans.jpa.modeler.specification.model.scene.JPAModelerScene;
import static org.netbeans.jpa.modeler.specification.model.util.JPAModelerUtil.ERROR_ICON_PATH;
import static org.netbeans.jpa.modeler.specification.model.util.JPAModelerUtil.SUCCESS_ICON_PATH;
import static org.netbeans.jpa.modeler.specification.model.util.JPAModelerUtil.WARNING_ICON_PATH;
import org.netbeans.modeler.core.ModelerFile;
import org.netbeans.modeler.properties.window.GenericDialog;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import static org.openide.awt.Mnemonics.setLocalizedText;
import static org.openide.util.NbBundle.getMessage;

/**
 *
 * @author Gaurav_Gupta
 */
public class GenerateCodeDialog extends GenericDialog
        implements PropertyChangeListener {

    /**
     * Java package root sources type.
     *
     * @see org.netbeans.api.project.Sources
     */
    private static final String SOURCES_TYPE_JAVA = "java"; // NOI18N

    private final Preferences technologyPref;
    private final FileObject modelerFileObject;
    private final String modelerFilePackage;
    private final EntityMappings entityMappings;
    private final ApplicationConfigData configData;
    private final JPAModelerScene scene;

    /**
     * Creates new form GenerateCodeDialog
     *
     * @param modelerFile
     */
    public GenerateCodeDialog(ModelerFile modelerFile) {
        this.scene = (JPAModelerScene) modelerFile.getModelerScene();
        this.configData = new ApplicationConfigData();
        this.modelerFileObject = modelerFile.getFileObject();
        this.entityMappings = (EntityMappings) modelerFile.getDefinitionElement();
        technologyPref = modelerFile.getProject() == null ? NbPreferences.forModule(Generator.class)
                : ProjectUtils.getPreferences(modelerFile.getProject(), ProjectUtils.class, true);
        propertyChangeSupport = new PropertyChangeSupport(this);
        propertyChangeSupport.addPropertyChangeListener(this);
        initComponents();
        manageGenerateButtonStatus();
        populateProjectCombo();
        setPackage(entityMappings.getPackage());
        this.setTitle(NbBundle.getMessage(GenerateCodeDialog.class, "GenerateCodeDialog.title"));
        getRootPane().setDefaultButton(generateSourceCode);
        modelerFilePackage = sourceGroup != null ? getPackageForFolder(sourceGroup, modelerFileObject.getParent()) : null;
        initLayer();
    }

    private void initLayer() {
        configPane.removeAll();
        configPane.setVisible(false);

        ProjectType projectType = getTargetPoject() != null ? ProjectHelper.getProjectType(getTargetPoject()) : null;
        if (projectType == null || projectType == ProjectType.JAR) {
            businessLayerCombo.setEnabled(false);
            controllerLayerCombo.setEnabled(false);
            viewerLayerCombo.setEnabled(false);
            this.pack();
            return;
        } else {
            businessLayerCombo.setEnabled(true);
        }

        businessLayerCombo.setModel(new DefaultComboBoxModel(Generator.getBusinessService().toArray()));
        if (projectType == ProjectType.WEB) {
            controllerLayerCombo.setModel(new DefaultComboBoxModel(new Object[]{new TechContext(new DefaultControllerLayer())}));
            viewerLayerCombo.setModel(new DefaultComboBoxModel(new Object[]{new TechContext(new DefaultViewerLayer())}));
        }
        controllerLayerCombo.setEnabled(false);
        viewerLayerCombo.setEnabled(false);

        TechContext businessContext = Generator.get(technologyPref.get(BUSINESS.name(), DefaultBusinessLayer.class.getSimpleName()));
        TechContext controllerContext = Generator.get(technologyPref.get(CONTROLLER.name(), DefaultControllerLayer.class.getSimpleName()));
        TechContext viewerContext = Generator.get(technologyPref.get(VIEWER.name(), DefaultViewerLayer.class.getSimpleName()));
        if (businessContext != null) {
            businessLayerCombo.setSelectedItem(businessContext);
            if (businessContext.isValid() && controllerContext != null && projectType == ProjectType.WEB) {
                controllerLayerCombo.setSelectedItem(controllerContext);
                if (controllerContext.isValid() && viewerContext != null) {
                    viewerLayerCombo.setSelectedItem(viewerContext);
                }
            }
        }
        this.pack();
    }

    private final LayerConfigPanel[] layerConfigPanels = new LayerConfigPanel[3];

   private void setTechPanel(TechContext techContext) {
            techContext.createPanel(targetPoject, sourceGroup, modelerFilePackage);
            configPane.removeAll();
            configPane.setVisible(false);
//          layerConfigPanels[index] = techPanel;
            switch (techContext.getTechnology().type()) {
                case BUSINESS:
                    getConfigData().setBussinesTechContext(techContext);
                    addLayerTab(getConfigData().getBussinesTechContext());
                    getConfigData().setControllerTechContext(null);
                    getConfigData().setViewerTechContext(null);
                    break;
                case CONTROLLER:
                    getConfigData().setControllerTechContext(techContext);
                    addLayerTab(getConfigData().getBussinesTechContext());
                    addLayerTab(getConfigData().getControllerTechContext());
                  getConfigData().setViewerTechContext(null);
                    break;
                case VIEWER:
                    getConfigData().setViewerTechContext(techContext);
                    addLayerTab(getConfigData().getBussinesTechContext());
                    addLayerTab(getConfigData().getControllerTechContext());
                    addLayerTab(getConfigData().getViewerTechContext());
                    break;
                default:
                    break;
            }

            if (configPane.getComponentCount() >= 1) {
                configPane.setSelectedComponent(techContext.getPanel());
                configPane.setVisible(true);
            }
            this.pack();
    }

    private void addLayerTab(TechContext techContext) {
        if (techContext.getTechnology().panel() != LayerConfigPanel.class) {//StringUtils.isBlank(title) || title.equalsIgnoreCase(NONE_LABEL)
            String title = techContext.getTechnology().label();
            configPane.addTab(title, techContext.getPanel());
            techContext.getSiblingTechContext().forEach(context -> this.addLayerTab(context));
        }
    }

    
    private void changeBusinessLayer(TechContext businessLayer) {
        ProjectType projectType = ProjectHelper.getProjectType(getTargetPoject());
        if (projectType == ProjectType.WEB) {
            controllerLayerCombo.setModel(new DefaultComboBoxModel(Generator.getController(businessLayer).toArray()));
            controllerLayerCombo.setEnabled(businessLayer.isValid());
            viewerLayerCombo.setModel(new DefaultComboBoxModel(new Object[]{new TechContext(new DefaultViewerLayer())}));
            viewerLayerCombo.setEnabled(false);
        }
        setTechPanel(businessLayer);
    }

    private void changeControllerLayer(TechContext controllerLayer) {
        viewerLayerCombo.setModel(new DefaultComboBoxModel(Generator.getViewer(controllerLayer).toArray()));
        viewerLayerCombo.setEnabled(controllerLayer.isValid());
        setTechPanel(controllerLayer);
    }

    private void changeViewerLayer(TechContext viewerLayer) {
        setTechPanel(viewerLayer);
    }

    private TechContext getBusinessLayer() {
        return (TechContext) businessLayerCombo.getModel().getSelectedItem();
    }

    private TechContext getViewerLayer() {
        return (TechContext) viewerLayerCombo.getModel().getSelectedItem();
    }

    private TechContext getControllerLayer() {
        return (TechContext) controllerLayerCombo.getModel().getSelectedItem();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        optionPane = new javax.swing.JLayeredPane();
        packageLabel = new javax.swing.JLabel();
        resourcePackageCombo = new javax.swing.JComboBox();
        businessLayerCombo = new javax.swing.JComboBox();
        businessLayerLabel = new javax.swing.JLabel();
        targetProjectCombo = new javax.swing.JComboBox();
        targetProjectLabel = new javax.swing.JLabel();
        sourceFolderCombo = new javax.swing.JComboBox();
        sourceFolderLabel = new javax.swing.JLabel();
        viewerLayerLabel = new javax.swing.JLabel();
        viewerLayerCombo = new javax.swing.JComboBox();
        controllerLayerCombo = new javax.swing.JComboBox();
        controllerLayerLabel = new javax.swing.JLabel();
        configPane = new javax.swing.JTabbedPane();
        actionPane = new javax.swing.JLayeredPane();
        actionLayeredPane = new javax.swing.JLayeredPane();
        generateSourceCode = new javax.swing.JButton();
        cencelGenerateCode = new javax.swing.JButton();
        entitySetting = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        org.openide.awt.Mnemonics.setLocalizedText(packageLabel, org.openide.util.NbBundle.getMessage(GenerateCodeDialog.class, "GenerateCodeDialog.packageLabel.text")); // NOI18N
        packageLabel.setPreferredSize(new java.awt.Dimension(150, 17));

        resourcePackageCombo.setEditable(true);
        resourcePackageCombo.setEditable(true);

        resourcePackageCombo.setEditable(true);
        businessLayerCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                businessLayerComboItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(businessLayerLabel, org.openide.util.NbBundle.getMessage(GenerateCodeDialog.class, "GenerateCodeDialog.businessLayerLabel.text")); // NOI18N

        targetProjectCombo.setMinimumSize(new java.awt.Dimension(50, 20));
        targetProjectCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                targetProjectComboItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(targetProjectLabel, org.openide.util.NbBundle.getMessage(GenerateCodeDialog.class, "GenerateCodeDialog.targetProjectLabel.text")); // NOI18N
        targetProjectLabel.setPreferredSize(new java.awt.Dimension(150, 17));

        sourceFolderCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                sourceFolderComboItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(sourceFolderLabel, org.openide.util.NbBundle.getMessage(GenerateCodeDialog.class, "GenerateCodeDialog.sourceFolderLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(viewerLayerLabel, org.openide.util.NbBundle.getMessage(GenerateCodeDialog.class, "GenerateCodeDialog.viewerLayerLabel.text")); // NOI18N

        resourcePackageCombo.setEditable(true);
        viewerLayerCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                viewerLayerComboItemStateChanged(evt);
            }
        });

        resourcePackageCombo.setEditable(true);
        controllerLayerCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                controllerLayerComboItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(controllerLayerLabel, org.openide.util.NbBundle.getMessage(GenerateCodeDialog.class, "GenerateCodeDialog.controllerLayerLabel.text")); // NOI18N

        optionPane.setLayer(packageLabel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        optionPane.setLayer(resourcePackageCombo, javax.swing.JLayeredPane.DEFAULT_LAYER);
        optionPane.setLayer(businessLayerCombo, javax.swing.JLayeredPane.DEFAULT_LAYER);
        optionPane.setLayer(businessLayerLabel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        optionPane.setLayer(targetProjectCombo, javax.swing.JLayeredPane.DEFAULT_LAYER);
        optionPane.setLayer(targetProjectLabel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        optionPane.setLayer(sourceFolderCombo, javax.swing.JLayeredPane.DEFAULT_LAYER);
        optionPane.setLayer(sourceFolderLabel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        optionPane.setLayer(viewerLayerLabel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        optionPane.setLayer(viewerLayerCombo, javax.swing.JLayeredPane.DEFAULT_LAYER);
        optionPane.setLayer(controllerLayerCombo, javax.swing.JLayeredPane.DEFAULT_LAYER);
        optionPane.setLayer(controllerLayerLabel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        optionPane.setLayer(configPane, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout optionPaneLayout = new javax.swing.GroupLayout(optionPane);
        optionPane.setLayout(optionPaneLayout);
        optionPaneLayout.setHorizontalGroup(
            optionPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, optionPaneLayout.createSequentialGroup()
                .addGroup(optionPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, optionPaneLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(configPane))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, optionPaneLayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(optionPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(optionPaneLayout.createSequentialGroup()
                                .addComponent(targetProjectLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(targetProjectCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(25, 25, 25)
                                .addComponent(sourceFolderLabel)
                                .addGap(18, 18, 18)
                                .addComponent(sourceFolderCombo, 0, 215, Short.MAX_VALUE))
                            .addGroup(optionPaneLayout.createSequentialGroup()
                                .addGroup(optionPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(viewerLayerLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(optionPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(controllerLayerLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(packageLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                                        .addComponent(businessLayerLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 95, Short.MAX_VALUE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(optionPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(controllerLayerCombo, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(businessLayerCombo, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(viewerLayerCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(resourcePackageCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))))
                .addContainerGap())
        );
        optionPaneLayout.setVerticalGroup(
            optionPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionPaneLayout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(optionPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(targetProjectLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(optionPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(sourceFolderLabel)
                        .addComponent(targetProjectCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(sourceFolderCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(10, 10, 10)
                .addGroup(optionPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(resourcePackageCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(packageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(optionPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(optionPaneLayout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(businessLayerLabel))
                    .addComponent(businessLayerCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(optionPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(optionPaneLayout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(controllerLayerLabel))
                    .addComponent(controllerLayerCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(optionPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(optionPaneLayout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(viewerLayerLabel))
                    .addComponent(viewerLayerCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(configPane, javax.swing.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE)
                .addContainerGap())
        );

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

        actionLayeredPane.setLayer(generateSourceCode, javax.swing.JLayeredPane.DEFAULT_LAYER);
        actionLayeredPane.setLayer(cencelGenerateCode, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout actionLayeredPaneLayout = new javax.swing.GroupLayout(actionLayeredPane);
        actionLayeredPane.setLayout(actionLayeredPaneLayout);
        actionLayeredPaneLayout.setHorizontalGroup(
            actionLayeredPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(actionLayeredPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(generateSourceCode, javax.swing.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cencelGenerateCode)
                .addContainerGap())
        );
        actionLayeredPaneLayout.setVerticalGroup(
            actionLayeredPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(actionLayeredPaneLayout.createSequentialGroup()
                .addGroup(actionLayeredPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(generateSourceCode)
                    .addComponent(cencelGenerateCode))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        actionPane.setLayer(actionLayeredPane, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout actionPaneLayout = new javax.swing.GroupLayout(actionPane);
        actionPane.setLayout(actionPaneLayout);
        actionPaneLayout.setHorizontalGroup(
            actionPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(actionPaneLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(actionLayeredPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2))
        );
        actionPaneLayout.setVerticalGroup(
            actionPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(actionPaneLayout.createSequentialGroup()
                .addComponent(actionLayeredPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        entitySetting.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/jpa/modeler/properties/resource/settings.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(entitySetting, org.openide.util.NbBundle.getMessage(GenerateCodeDialog.class, "GenerateCodeDialog.entitySetting.text")); // NOI18N
        entitySetting.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                entitySettingActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(entitySetting)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(actionPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(23, 23, 23))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(optionPane)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(optionPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(actionPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(entitySetting))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    public final static String PROP_TARGET_PROJECT = "TARGET_PROJECT"; // NOI18N
    public final static String PROP_NO_TARGET_PROJECT = "NO_TARGET_PROJECT"; // NOI18N
    public final static String PROP_SOURCE_FOLDER = "SOURCE_FOLDER"; // NOI18N
    public final static String PROP_NO_SOURCE_FOLDER = "NO_SOURCE_FOLDER"; // NOI18N

    private Project targetPoject = null;
    private final Project orignalProject = null;
    private SourceGroup sourceGroup = null;
    private boolean noTargetProject = false;
    private boolean noOpenTargets = false;

    private void targetProjectComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_targetProjectComboItemStateChanged
        setTargetPoject((Project) targetProjectCombo.getSelectedItem());
        populateSourceFolderCombo();
        initLayer();
        String prop = getTargetPoject() == null ? PROP_NO_TARGET_PROJECT : PROP_TARGET_PROJECT;
        getPropertyChangeSupport().firePropertyChange(prop, null, evt);
    }//GEN-LAST:event_targetProjectComboItemStateChanged

    private void sourceFolderComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_sourceFolderComboItemStateChanged
        setSourceGroup((SourceGroup) sourceFolderCombo.getSelectedItem());
        populatePackageCombo();
    }//GEN-LAST:event_sourceFolderComboItemStateChanged

    private void generateSourceCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateSourceCodeActionPerformed
        if (!hasError()) {
            setVisible(false);
            this.setDialogResult(javax.swing.JOptionPane.OK_OPTION);
            dispose();
            store();
        }
    }//GEN-LAST:event_generateSourceCodeActionPerformed

    private boolean hasError() {
        if (sourceGroup == null) {
            NotifyDescriptor d = new NotifyDescriptor.Message("Please select the Source Folder .", NotifyDescriptor.INFORMATION_MESSAGE);
            d.setTitle("Source Folder");
            DialogDisplayer.getDefault().notify(d);
            return true;
        }
        if (!SourceVersion.isName(getPackage())) {
            NotifyDescriptor d = new NotifyDescriptor.Message("Please select the Entity Package .", NotifyDescriptor.INFORMATION_MESSAGE);
            d.setTitle("Entity Package");
            DialogDisplayer.getDefault().notify(d);
            return true;
        }
        for (Component component : configPane.getComponents()) {
            if (component instanceof LayerConfigPanel) {
                LayerConfigPanel panel = (LayerConfigPanel) component;
                if (panel.hasError()) {
                    configPane.setSelectedComponent(component);
                    return true;
                } else {
                    panel.store();
                }
            }
        }
        return false;
    }

    private void store() {
        entityMappings.setPackage(getPackage());
        if (getBusinessLayer() != null) {
            technologyPref.put(BUSINESS.name(), getBusinessLayer().getGenerator().getClass().getSimpleName());
            if (getControllerLayer() != null) {
                technologyPref.put(CONTROLLER.name(), getControllerLayer().getGenerator().getClass().getSimpleName());
                if (getViewerLayer() != null) {
                    technologyPref.put(VIEWER.name(), getViewerLayer().getGenerator().getClass().getSimpleName());
                }
            }
        }
    }

    private void cencelGenerateCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cencelGenerateCodeActionPerformed
        cancelActionPerformed(evt);
    }//GEN-LAST:event_cencelGenerateCodeActionPerformed

    private void businessLayerComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_businessLayerComboItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            changeBusinessLayer(getBusinessLayer());
        }
    }//GEN-LAST:event_businessLayerComboItemStateChanged

    private void viewerLayerComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_viewerLayerComboItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            changeViewerLayer(getViewerLayer());
        }
    }//GEN-LAST:event_viewerLayerComboItemStateChanged

    private void controllerLayerComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_controllerLayerComboItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            changeControllerLayer(getControllerLayer());
        }
    }//GEN-LAST:event_controllerLayerComboItemStateChanged

    private void entitySettingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_entitySettingActionPerformed
        EntityGenerationSettingDialog dialog = new EntityGenerationSettingDialog(scene, entityMappings);
        dialog.setVisible(true);
        manageGenerateButtonStatus();
    }//GEN-LAST:event_entitySettingActionPerformed

    private void manageGenerateButtonStatus() {
        List<JavaClass> javaClassList = entityMappings.getAllJavaClass().stream().filter(c -> c.getGenerateSourceCode()).collect(toList());
        if (javaClassList.isEmpty()) {
            generateSourceCode.setIcon(new javax.swing.ImageIcon(getClass().getResource(ERROR_ICON_PATH)));
            setLocalizedText(generateSourceCode, getMessage(GenerateCodeDialog.class, "GenerateCodeDialog.generateSourceCode.text")); // NOI18N
            generateSourceCode.setEnabled(false);
        } else if (javaClassList.size() < entityMappings.getAllJavaClass().size()) {
            generateSourceCode.setIcon(new javax.swing.ImageIcon(getClass().getResource(WARNING_ICON_PATH)));
            setLocalizedText(generateSourceCode, getMessage(GenerateCodeDialog.class, "GenerateCodeDialog.generateSourceCode.warning.text")); // NOI18N
            generateSourceCode.setEnabled(true);
        } else {
            generateSourceCode.setIcon(new javax.swing.ImageIcon(getClass().getResource(SUCCESS_ICON_PATH)));
            setLocalizedText(generateSourceCode, getMessage(GenerateCodeDialog.class, "GenerateCodeDialog.generateSourceCode.text")); // NOI18N
            generateSourceCode.setEnabled(true);
        }
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent event) {
//        String propName = "";
//
//        if (event != null) {
//            propName = event.getPropertyName();
//        }
//
//        if (propName.equals(PROP_TARGET_PROJECT)) {
//            noTargetProject = false;
//        } else if (propName.equals(PROP_NO_TARGET_PROJECT)) {
//            noTargetProject = true;
//        }

//        String msg;
//
//        if (noOpenTargets) {
//            msg = NbBundle.getMessage(
//                    GenerateCodeDialog.class, "MSG_NoOpenTargets"); // NIO18N
//        } else if (noTargetProject) {
//            msg = NbBundle.getMessage(
//                    GenerateCodeDialog.class, "MSG_NoTargetJavaProject"); // NIO18N
//        }

    }

    //
    // target project added elements processing
    //
    private void enableExistingProjectElementGroup(boolean enable) {
        targetProjectCombo.setEnabled(enable);
        sourceFolderCombo.setEnabled(enable);
    }

    private void populateProjectCombo() {
        ProjectCellRenderer projectCellRenderer = new ProjectCellRenderer(targetProjectCombo.getRenderer());
        targetProjectCombo.setRenderer(projectCellRenderer);
        List<Project> list = getJavaProjects();

        if (orignalProject != null && !list.contains(orignalProject)) {
            list.add(orignalProject);
        }

        if (list == null || list.isEmpty()) {
            noOpenTargets = true;
            enableExistingProjectElementGroup(false);
        } else {
            DefaultComboBoxModel projectsModel = new DefaultComboBoxModel(list.toArray());
            targetProjectCombo.setModel(projectsModel);

            // Issue Fix #5850 
            Project project = FileOwnerQuery.getOwner(modelerFileObject);
            if (project != null) {
                targetProjectCombo.setSelectedItem(project);
            } else {
                targetProjectCombo.setSelectedIndex(-1);
            }

            // When the selected index was set to -1 it reset the targetPrj
            // value.  Since the targetPrj was simply initialized with the
            // origPrj value, just set it again.
            setTargetPoject(orignalProject);
            selectTargetProject();
            noOpenTargets = false;
            // enableExistingProjectElementGroup(true);
        }

        propertyChange(null);
    }

    private void selectTargetProject() {
        if (getTargetPoject() == null) {
            if (targetProjectCombo.getSelectedItem() != null) {
                setTargetPoject((Project) targetProjectCombo.getSelectedItem());
                sourceFolderCombo.setEnabled(true);
            } else {
                sourceFolderCombo.setEnabled(false);
            }
        } else {
            targetProjectCombo.setSelectedItem(getTargetPoject());
            sourceFolderCombo.setEnabled(true);
        }

        if (targetProjectCombo.getSelectedItem() != null) {
            populateSourceFolderCombo();
            sourceFolderCombo.setEnabled(true);
        }
    }

    private void populateSourceFolderCombo() {
        SourceRootCellRenderer srcCellRenderer = new SourceRootCellRenderer(sourceFolderCombo.getRenderer());
        sourceFolderCombo.setRenderer(srcCellRenderer);
        ArrayList<SourceGroup> srcRoots = new ArrayList<>();
        int index = 0;
        FileObject sfo = getSourceGroup() != null? getSourceGroup().getRootFolder() : null;
        if (targetPoject != null) {
            Sources sources = ProjectUtils.getSources(targetPoject);
            if (sources != null) {
                SourceGroup[] srcGrps = sources.getSourceGroups(SOURCES_TYPE_JAVA);
                if (srcGrps != null) {
                    for (SourceGroup g : srcGrps) {
                        if (g != null) {
                            srcRoots.add(g);
                            if (g.getRootFolder() != null && g.getRootFolder().equals(sfo)) {
                                index = srcRoots.size() - 1;
                            }
                        }
                    }
                }
            }
        }

        DefaultComboBoxModel rootsModel  = new DefaultComboBoxModel(srcRoots.toArray());
        sourceFolderCombo.setModel(rootsModel);
        if (srcRoots.size() > 0) {
            sourceFolderCombo.setSelectedIndex(index);
            setSourceGroup(srcRoots.get(index));
            sourceFolderCombo.setEnabled(true);
        } else {
            sourceFolderCombo.setEnabled(false);
        }
        populatePackageCombo();
    }

    private void populatePackageCombo() {
        if (sourceGroup != null) {
            resourcePackageCombo.setRenderer(PackageView.listRenderer());
            ComboBoxModel model = PackageView.createListView(sourceGroup);
            if (model.getSize() > 0) {
                model.setSelectedItem(model.getElementAt(0));
            }
            resourcePackageCombo.setModel(model);
        }
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
     * @return the targetPoject
     */
    public Project getTargetPoject() {
        return targetPoject;
    }

    /**
     * @param targetPoject the targetPoject to set
     */
    public void setTargetPoject(Project targetPoject) {
        this.targetPoject = targetPoject;
    }

    public String getPackage() {
        return ((JTextComponent) resourcePackageCombo.getEditor().getEditorComponent()).getText();
    }

    private void setPackage(String _package) {
        ComboBoxModel model = resourcePackageCombo.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            if (model.getElementAt(i).toString().equals(_package)) {
                model.setSelectedItem(model.getElementAt(i));
                return;
            }
        }
        ((JTextComponent) resourcePackageCombo.getEditor().getEditorComponent()).setText(_package);
    }

    private PropertyChangeSupport propertyChangeSupport = null;

    public PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLayeredPane actionLayeredPane;
    private javax.swing.JLayeredPane actionPane;
    private javax.swing.JComboBox businessLayerCombo;
    private javax.swing.JLabel businessLayerLabel;
    private javax.swing.JButton cencelGenerateCode;
    private javax.swing.JTabbedPane configPane;
    private javax.swing.JComboBox controllerLayerCombo;
    private javax.swing.JLabel controllerLayerLabel;
    private javax.swing.JButton entitySetting;
    private javax.swing.JButton generateSourceCode;
    private javax.swing.JLayeredPane optionPane;
    private javax.swing.JLabel packageLabel;
    private javax.swing.JComboBox resourcePackageCombo;
    private javax.swing.JComboBox sourceFolderCombo;
    private javax.swing.JLabel sourceFolderLabel;
    private javax.swing.JComboBox targetProjectCombo;
    private javax.swing.JLabel targetProjectLabel;
    private javax.swing.JComboBox viewerLayerCombo;
    private javax.swing.JLabel viewerLayerLabel;
    // End of variables declaration//GEN-END:variables

    /**
     * @return the configData
     */
    public ApplicationConfigData getConfigData() {
        configData.setProject(getTargetPoject());
        configData.setSourceGroup(getSourceGroup());
        return configData;
    }

}
