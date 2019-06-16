package org.fogbowcloud.saps.engine.core.dispatcher;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * DTO of parameters from a {@link Submission}.
 */
public class SubmissionParameters {

    private static final DateFormat DATE_FORMATER = new SimpleDateFormat("yyyy-MM-dd");

    private final String lowerLeftLatitude;

    private final String lowerLeftLongitude;

    private final String upperRightLatitude;

    private final String upperRightLongitude;

    private final Date initDate;

    private final Date endDate;

    private final String inputGathering;

    private final String inputPreprocessing;

    private final String algorithmExecution;

    public SubmissionParameters(String lowerLeftLatitude, String lowerLeftLongitude,
                                String upperRightLatitude, String upperRightLongitude,
                                Date initDate, Date endDate, String inputGathering,
                                String inputPreprocessing, String algorithmExecution) {
        this.lowerLeftLatitude = lowerLeftLatitude;
        this.lowerLeftLongitude = lowerLeftLongitude;
        this.upperRightLatitude = upperRightLatitude;
        this.upperRightLongitude = upperRightLongitude;
        this.initDate = initDate;
        this.endDate = endDate;
        this.inputGathering = inputGathering;
        this.inputPreprocessing = inputPreprocessing;
        this.algorithmExecution = algorithmExecution;
    }

    /**
     * @return the lowerLeftLatitude
     */
    public String getLowerLeftLatitude() {
        return lowerLeftLatitude;
    }

    /**
     * @return the lowerLeftLongitude
     */
    public String getLowerLeftLongitude() {
        return lowerLeftLongitude;
    }

    /**
     * @return the upperRightLatitude
     */
    public String getUpperRightLatitude() {
        return upperRightLatitude;
    }

    /**
     * @return the upperRightLongitude
     */
    public String getUpperRightLongitude() {
        return upperRightLongitude;
    }

    /**
     * @return the initDate
     */
    public Date getInitDate() {
        return initDate;
    }

    /**
     * @return the endDate
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * @return the inputGathering
     */
    public String getInputGathering() {
        return inputGathering;
    }

    /**
     * @return the inputPreprocessing
     */
    public String getInputPreprocessing() {
        return inputPreprocessing;
    }

    /**
     * @return the algorithmExecution
     */
    public String getAlgorithmExecution() {
        return algorithmExecution;
    }

    /**
     * @return the Form URL Encoded representation of this SubmissionParameters.
     */
    public String toFormUrlEncoded() {
        return String.format("%s=%s&", "lowerLeft[]", lowerLeftLatitude) +
                String.format("%s=%s&", "lowerLeft[]", lowerLeftLongitude) +
                String.format("%s=%s&", "upperRight[]", upperRightLatitude) +
                String.format("%s=%s&", "upperRight[]", upperRightLongitude) +
                String.format("%s=%s&", "initialDate", DATE_FORMATER.format(initDate)) +
                String.format("%s=%s&", "finalDate", DATE_FORMATER.format(endDate)) +
                String.format("%s=%s&", "inputGatheringTag", inputGathering) +
                String.format("%s=%s&", "inputPreprocessingTag", inputPreprocessing) +
                String.format("%s=%s", "algorithmExecutionTag", algorithmExecution);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((algorithmExecution == null) ? 0 : algorithmExecution.hashCode());
        result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
        result = prime * result + ((initDate == null) ? 0 : initDate.hashCode());
        result = prime * result + ((inputGathering == null) ? 0 : inputGathering.hashCode());
        result = prime * result + ((inputPreprocessing == null) ? 0 : inputPreprocessing.hashCode());
        result = prime * result + ((lowerLeftLatitude == null) ? 0 : lowerLeftLatitude.hashCode());
        result = prime * result + ((lowerLeftLongitude == null) ? 0 : lowerLeftLongitude.hashCode());
        result = prime * result + ((upperRightLatitude == null) ? 0 : upperRightLatitude.hashCode());
        result = prime * result + ((upperRightLongitude == null) ? 0 : upperRightLongitude.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SubmissionParameters other = (SubmissionParameters) obj;
        if (algorithmExecution == null) {
            if (other.algorithmExecution != null)
                return false;
        } else if (!algorithmExecution.equals(other.algorithmExecution))
            return false;
        if (endDate == null) {
            if (other.endDate != null)
                return false;
        } else if (!endDate.equals(other.endDate))
            return false;
        if (initDate == null) {
            if (other.initDate != null)
                return false;
        } else if (!initDate.equals(other.initDate))
            return false;
        if (inputGathering == null) {
            if (other.inputGathering != null)
                return false;
        } else if (!inputGathering.equals(other.inputGathering))
            return false;
        if (inputPreprocessing == null) {
            if (other.inputPreprocessing != null)
                return false;
        } else if (!inputPreprocessing.equals(other.inputPreprocessing))
            return false;
        if (lowerLeftLatitude == null) {
            if (other.lowerLeftLatitude != null)
                return false;
        } else if (!lowerLeftLatitude.equals(other.lowerLeftLatitude))
            return false;
        if (lowerLeftLongitude == null) {
            if (other.lowerLeftLongitude != null)
                return false;
        } else if (!lowerLeftLongitude.equals(other.lowerLeftLongitude))
            return false;
        if (upperRightLatitude == null) {
            if (other.upperRightLatitude != null)
                return false;
        } else if (!upperRightLatitude.equals(other.upperRightLatitude))
            return false;
        if (upperRightLongitude == null) {
            if (other.upperRightLongitude != null)
                return false;
        } else if (!upperRightLongitude.equals(other.upperRightLongitude))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SubmissionParameters [algorithmExecution=" + algorithmExecution + ", endDate=" + endDate + ", initDate="
                + initDate + ", inputGathering=" + inputGathering + ", inputPreprocessing=" + inputPreprocessing
                + ", lowerLeftLatitude=" + lowerLeftLatitude + ", lowerLeftLongitude=" + lowerLeftLongitude
                + ", upperRightLatitude=" + upperRightLatitude + ", upperRightLongitude=" + upperRightLongitude + "]";
    }

}
