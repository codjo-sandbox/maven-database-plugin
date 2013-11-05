/*
 * Team : CODJO / OSI / SI / BO
 *
 * Copyright (c) 2001 CODJO.
 */
package net.codjo.maven.mojo.database;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
/**
 *
 */
public class DefaultProductVersion implements ProductVersion {
    private String applicationDeliveryDirectory;
    private boolean inProduction = false;


    public DefaultProductVersion(String applicationName) {
        this.applicationDeliveryDirectory = "C:\\"+applicationName;
    }


    public String getProductVersion() {
        Properties versionProperties = new Properties();
        try {
            File file = new File(getApplicationDeliveryDirectory(applicationDeliveryDirectory));
            versionProperties.load(new FileInputStream(file + "\\version.properties"));
            return versionProperties.getProperty("tag");
        }
        catch (IOException e) {
            ;
        }
        return null;
    }


    public String getApplicationDeliveryDirectory(String applicationDirectory) {
        String deliveryProduction = applicationDirectory + "\\PRODUCTION\\CLIENT";
        String deliveryUAT = applicationDirectory + "\\RECETTE\\CLIENT";

        if (applicationDeliveryDirectory == null) {
            throw new UnsupportedOperationException("Le nom de l'application n'est pas fourni !");
        }

        if (new File(deliveryProduction + "\\version.properties").exists()) {
            inProduction = true;
            return deliveryProduction;
        }
        else if (new File(deliveryUAT + "\\version.properties").exists()) {
            return deliveryUAT;
        }
        else {
            throw new UnsupportedOperationException(
                  "Le fichier version.properties est introuvable sur le répertoire de production et de recette."+applicationDirectory);
        }
    }


    public void setApplicationDeliveryDirectory(String applicationDeliveryDirectory) {
        this.applicationDeliveryDirectory = applicationDeliveryDirectory;
    }


    public boolean isInProduction() {
        return inProduction;
    }


    public void setInProduction(boolean inProduction) {
        this.inProduction = inProduction;
    }
}
