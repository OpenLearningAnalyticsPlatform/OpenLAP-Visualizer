package de.rwthaachen.openlap.visualizer.core.service;

import DataSet.OLAPPortConfiguration;
import de.rwthaachen.openlap.visualizer.core.dao.VisualizationFrameworkRepository;
import de.rwthaachen.openlap.visualizer.core.dao.VisualizationMethodRepository;
import de.rwthaachen.openlap.visualizer.core.exceptions.DataSetValidationException;
import de.rwthaachen.openlap.visualizer.core.exceptions.FileManagerException;
import de.rwthaachen.openlap.visualizer.core.exceptions.VisualizationFrameworkDeletionException;
import de.rwthaachen.openlap.visualizer.core.exceptions.VisualizationFrameworksUploadException;
import de.rwthaachen.openlap.visualizer.core.framework.VisualizationCodeGenerator;
import de.rwthaachen.openlap.visualizer.core.framework.factory.VisualizationCodeGeneratorFactory;
import de.rwthaachen.openlap.visualizer.core.framework.factory.VisualizationCodeGeneratorFactoryImpl;
import de.rwthaachen.openlap.visualizer.core.framework.validators.VisualizationFrameworksUploadValidator;
import de.rwthaachen.openlap.visualizer.core.model.DataTransformerMethod;
import de.rwthaachen.openlap.visualizer.core.model.VisualizationFramework;
import de.rwthaachen.openlap.visualizer.core.model.VisualizationMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * A service which provides functions to perform CRUD operations on the Visualization Frameworks or Visualization Methods. In addition, also methods to get information about the stored
 * frameworks
 *
 * @author Bassim Bashir
 */
@Service
public class VisualizationFrameworkService {

    @Autowired
    private VisualizationFrameworkRepository visualizationFrameworkRepository;

    @Autowired
    private VisualizationMethodRepository visualizationMethodRepository;

    @Autowired
    private FileManager fileManager;

    @Autowired
    private ConfigurationService configurationService;

    /**
     * @return The list of visualization frameworks existing in the system
     */
    public List<VisualizationFramework> findAllVisualizationFrameworks() {
        List<VisualizationFramework> visualizationFrameworkList = new ArrayList<>();
        visualizationFrameworkRepository.findAll().forEach(visualizationFrameworkList::add);
        return visualizationFrameworkList;
    }

    /**
     * @param visualizationFrameworkId The id of the framework to retrieve
     * @return The visualization framework represented by the provided id
     */
    public VisualizationFramework findVisualizationFrameworkById(long visualizationFrameworkId) {
        return visualizationFrameworkRepository.findOne(visualizationFrameworkId);
    }

    /**
     * @param visualizationMethodId The id of the visualization method to retrieve
     * @return The visualization method represented by the provided id
     */
    public VisualizationMethod findVisualizationMethodById(long visualizationMethodId) {
        return visualizationMethodRepository.findOne(visualizationMethodId);
    }

    /**
     * Performs the upload of the visualization framework by copying over the jar bundle and making the relevant Database entries
     *
     * @param frameworkList The configuration of the frameworks in the provided jar file
     * @param jarFile       The jar bundle which contains the package framework implementation
     * @throws VisualizationFrameworksUploadException If the validation of the provided configuration failed or copying the provided jar file was not successful
     */
    public void uploadVisualizationFrameworks(List<VisualizationFramework> frameworkList, MultipartFile jarFile) throws VisualizationFrameworksUploadException {
        // set the file location in all the framework objects
        frameworkList.forEach(framework -> framework.setFrameworkLocation(Paths.get(configurationService.getVisualizationFrameworksJarStorageLocation(), jarFile.getName() + configurationService.getJarBundleExtension()).toAbsolutePath().toString()));
        //validate the jar classes first before passing it on to the File Manager
        VisualizationFrameworksUploadValidator visualizationFrameworksUploadValidator = new VisualizationFrameworksUploadValidator();
        if (visualizationFrameworksUploadValidator.validateVisualizationFrameworksUploadConfiguration(frameworkList, jarFile)) {
            //if the configuration is valid then perform the upload, i.e db entries and copying of the jar
            // first copy the file over
            try {
                FileManager fileManager = new FileManager();
                fileManager.saveFile(jarFile.getName(), jarFile);
                visualizationFrameworkRepository.save(frameworkList);
            } catch (FileManagerException fileManagerException) {
                throw new VisualizationFrameworksUploadException(fileManagerException.getMessage());
            }
        } else {
            throw new VisualizationFrameworksUploadException("Upload configuration is not correct");
        }
    }

    /**
     * Removes a previously added visualization framework from the system. This includes all the Database entries alongwith the
     * stored JAR
     *
     * @param frameworkId The id of the framework to delete. The function will first try
     *                    to parse the parameter into an id if that doesn't then to treat it as a framework name
     * @return true if the deletion of the framework was successful
     * @throws VisualizationFrameworkDeletionException if the deletion of the framework encountered problems such as the file couldn't be removed
     */
    @Transactional(rollbackFor = {VisualizationFrameworkDeletionException.class})
    public void deleteVisualizationFramework(String frameworkId) throws VisualizationFrameworkDeletionException {
        if (frameworkId == null || frameworkId.isEmpty())
            throw new VisualizationFrameworkDeletionException("The id of the framework to delete should be null or empty string");
        try {
            long idOfFramework = Long.parseLong(frameworkId);
            //first load the framework to get the jar location
            VisualizationFramework visualizationFramework = visualizationFrameworkRepository.findOne(idOfFramework);
            if (visualizationFramework == null)
                throw new VisualizationFrameworkDeletionException("Could not delete the framework with id: " + frameworkId + ", not found");
            try {
                fileManager.deleteFile(visualizationFramework.getFrameworkLocation());
            } catch (FileManagerException fileManagerException) {
                throw new VisualizationFrameworkDeletionException(fileManagerException.getMessage());
            }
            // finally remove all the database entries
            visualizationFrameworkRepository.delete(idOfFramework);
        } catch (NumberFormatException numberFormatException) {
            throw new VisualizationFrameworkDeletionException("The id: " + frameworkId + " is not parseable, should be a long value");
        }
    }

    /**
     * Updates the attributes of visualization method
     *
     * @param newAttributes An instance filled with the new values of the visualization method. Except for the visualization framework, the method id and the data transformer id
     *                      all other attributes can be updated
     * @param idOfMethod    the id of the visualization method to be updated
     */
    public void updateVisualizationMethodAttributes(VisualizationMethod newAttributes, long idOfMethod) {
        VisualizationMethod visualizationMethod = visualizationMethodRepository.findOne(idOfMethod);
        // update the name of the method
        if (newAttributes.getName() != null && !newAttributes.getName().isEmpty())
            visualizationMethod.setName(newAttributes.getName());

        // update the implementing class
        if (newAttributes.getImplementingClass() != null && !newAttributes.getImplementingClass().isEmpty())
            visualizationMethod.setImplementingClass(newAttributes.getImplementingClass());

        DataTransformerMethod dataTransformerMethod = visualizationMethod.getDataTransformerMethod();

        if (newAttributes.getDataTransformerMethod().getName() != null && !newAttributes.getDataTransformerMethod().getName().isEmpty())
            dataTransformerMethod.setName(newAttributes.getDataTransformerMethod().getName());

        if (newAttributes.getDataTransformerMethod().getImplementingClass() != null && !newAttributes.getDataTransformerMethod().getImplementingClass().isEmpty())
            dataTransformerMethod.setImplementingClass(newAttributes.getDataTransformerMethod().getImplementingClass());

        //finally set the data transformer method
        visualizationMethod.setDataTransformerMethod(dataTransformerMethod);

        //commit the changes
        visualizationMethodRepository.save(visualizationMethod);
    }

    /**
     * Updates the attributes of visualization framework
     *
     * @param newAttributes An instance filled with the new values of the visualization framework. Only the attributes namely, description and uploadedBy can be
     *                      updated
     * @param idOfFramework the id of the visualization framework to be updated
     */
    public void updateVisualizationFrameworkAttributes(VisualizationFramework newAttributes, long idOfFramework) {
        VisualizationFramework visualizationFramework = visualizationFrameworkRepository.findOne(idOfFramework);

        if (newAttributes.getDescription() != null && !newAttributes.getDescription().isEmpty())
            visualizationFramework.setDescription(newAttributes.getDescription());
        if (newAttributes.getCreator() != null && !newAttributes.getCreator().isEmpty())
            visualizationFramework.setCreator(newAttributes.getCreator());

        visualizationFrameworkRepository.save(visualizationFramework);
    }

    /**
     * Validates the configuration of the visualization method (i.e. the inputs that it accepts) with the provided OLAPPortConfiguration.
     *
     * @param visualizationMethodId The id of the method for which to validate the configuration
     * @param olapPortConfiguration The OLAPPortConfiguration against which to validate the method configuration
     * @return true if the the provided port configuration matches the configuration of the visualization method
     * @throws DataSetValidationException If the validation encountered an error
     */
    public boolean validateVisualizationMethodConfiguration(long visualizationMethodId, OLAPPortConfiguration olapPortConfiguration) throws DataSetValidationException {
        VisualizationMethod visualizationMethod = visualizationMethodRepository.findOne(visualizationMethodId);
        if (visualizationMethod != null) {
            //ask the factories for the instance
            VisualizationCodeGeneratorFactory visualizationCodeGeneratorFactory = new VisualizationCodeGeneratorFactoryImpl(visualizationMethod.getVisualizationFramework().getFrameworkLocation());
            VisualizationCodeGenerator codeGenerator = visualizationCodeGeneratorFactory.createVisualizationCodeGenerator(visualizationMethod.getImplementingClass());
            return codeGenerator.isDataProcessable(olapPortConfiguration);
        } else {
            throw new DataSetValidationException("The visualization method represented by the id: " + visualizationMethodId + " not found.");
        }
    }
}