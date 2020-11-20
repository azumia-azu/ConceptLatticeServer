package fca.exception;

import fca.messages.ExceptionMessages;

public class GTreeConstructionException extends LatticeMinerException {
    /**
     * Constructeur
     *
     * @param moreInformation le message détaillé
     */
    public GTreeConstructionException(String moreInformation) {
        super(moreInformation);
    }

    @Override
    public String getMessageGeneral() {
        return ExceptionMessages.getString("GTreeConstructionException");
    }
}
