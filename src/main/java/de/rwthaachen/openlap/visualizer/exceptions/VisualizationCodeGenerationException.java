package de.rwthaachen.openlap.visualizer.exceptions;

/**
 * Created by bas on 1/13/16.
 */
public class VisualizationCodeGenerationException extends BaseException {

    public VisualizationCodeGenerationException(String message) {
        super(message,VisualizationCodeGenerationException.class.getSimpleName(),"");
    }
}

