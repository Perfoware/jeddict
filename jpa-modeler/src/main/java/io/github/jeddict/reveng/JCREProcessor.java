/**
 * Copyright 2013-2018 the original author or authors from the Jeddict project (https://jeddict.github.io/).
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
package io.github.jeddict.reveng;

import java.io.File;
import java.util.List;
import org.netbeans.modeler.core.ModelerFile;

/**
 *
 * @author Gaurav Gupta
 */
public interface JCREProcessor {

    /**
     * Update the complete existing diagram with existing class
     *
     * @param modelerFile
     */
    void syncExistingDiagram(ModelerFile modelerFile);

    /**
     * Drop classes in existing diagram
     *
     * @param modelerFile
     * @param entityFiles
     */
    void processDropedClasses(ModelerFile modelerFile, List<File> entityFiles);

    /**
     * Drop doc in existing diagram
     *
     * @param modelerFile
     * @param docFile
     */
    void processDropedDocument(ModelerFile modelerFile, File docFile);
}
