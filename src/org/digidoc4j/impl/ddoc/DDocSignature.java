/* DigiDoc4J library
*
* This software is released under either the GNU Library General Public
* License (see LICENSE.LGPL).
*
* Note that the only valid version of the LGPL license as far as this
* project is concerned is the original GNU Library General Public License
* Version 2.1, February 1999
*/

package org.digidoc4j.impl.ddoc;

import ee.sk.digidoc.CertValue;
import org.digidoc4j.Signature;
import org.digidoc4j.SignatureValidationResult;
import org.digidoc4j.X509Cert;
import org.digidoc4j.exceptions.DigiDoc4JException;
import org.digidoc4j.exceptions.NotYetImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.digidoc4j.SignatureProfile;

/**
 * Signature implementation. Provides an interface for handling a signature and the
 * corresponding OCSP response properties.
 */
public class DDocSignature implements Signature {
  private static final Logger logger = LoggerFactory.getLogger(DDocSignature.class);
  private X509Cert certificate;
  private final ee.sk.digidoc.Signature origin;
  private int indexInArray = 0;

  /**
   * @param signature add description
   */
  public DDocSignature(ee.sk.digidoc.Signature signature) {
    logger.debug("");
    this.origin = signature;
  }

  public void setCertificate(X509Cert cert) {
    logger.debug("");
    this.certificate = cert;
  }

  @Override
  public String getCity() {
    logger.debug("getCity");
    
    String city = null;
    if (origin.getSignedProperties().getSignatureProductionPlace() != null) {
      city = origin.getSignedProperties().getSignatureProductionPlace().getCity();
    }
    
    return city == null? "" : city;
  }

  @Override
  public String getCountryName() {
    logger.debug("getCountryName");
    
    String countryName = null;
    if (origin.getSignedProperties().getSignatureProductionPlace() != null) {
      countryName = origin.getSignedProperties().getSignatureProductionPlace().getCountryName();
    }
    
    return countryName == null? "" : countryName;
  }

  @Override
  public String getId() {
    logger.debug("getId");

    String id = "";
    if(origin.getId() != null){
      id =  origin.getId();
    }

    return id;
  }

  @Override
  public byte[] getOCSPNonce() {
    logger.debug("getOCSPNonce");
    return null;
  }

  @Override
  public X509Cert getOCSPCertificate() {
    logger.debug("getOCSPCertificate");
    X509Certificate x509Certificate = origin.findResponderCert();
    return x509Certificate != null ? new X509Cert(x509Certificate) : null;
  }

  @Override
  @Deprecated
  public String getPolicy() {
    logger.debug("getPolicy");
    return "";
  }

  @Override
  public String getPostalCode() {

    String postalCode = null;
    if (origin.getSignedProperties().getSignatureProductionPlace() != null) {
      postalCode = origin.getSignedProperties().getSignatureProductionPlace().getPostalCode();
    }
    logger.debug("Postal code: " + postalCode);
    return postalCode == null? "" : postalCode;
  }

  /**
   * This method returns Date object, it can be null.
   *
   * @return Date
   */
  @Override
  public Date getOCSPResponseCreationTime() {
    Date date = origin.getSignatureProducedAtTime();
    logger.debug("OCSP response creation time: " + date);
    return date;
  }

  /**
   * This method returns Date object, it can be null.
   *
   * @return Date
   */
  @Override
  @Deprecated
  public Date getProducedAt() {
    return getOCSPResponseCreationTime();
  }

  @Override
  public Date getTimeStampCreationTime() {
    logger.warn("Not yet implemented");
    throw new NotYetImplementedException();
  }

  /**
   * This method returns Date object, it can be null.
   *
   * @return Date
   */
  @Override
  public Date getTrustedSigningTime() {
    return getOCSPResponseCreationTime();
  }

  @Override
  public SignatureProfile getProfile() {
    logger.debug("Profile is LT_TM");
    return SignatureProfile.LT_TM;
  }

  @Override
  public String getSignatureMethod() {
    logger.debug("getSignatureMethod");
    String signatureMethod = origin.getSignedInfo().getSignatureMethod();
    logger.debug("Signature method: " + signatureMethod);
    return signatureMethod == null ? "" : signatureMethod;
  }

  @Override
  public List<String> getSignerRoles() {
    logger.debug("getSignerRoles");
    List<String> roles = new ArrayList<>();
    int numberOfRoles = origin.getSignedProperties().countClaimedRoles();
    for (int i = 0; i < numberOfRoles; i++) {
      roles.add(origin.getSignedProperties().getClaimedRole(i));
    }
    return roles;
  }

  @Override
  public X509Cert getSigningCertificate() {
    logger.debug("getSigningCertificate");
    return certificate;
  }

  @Override
  public Date getClaimedSigningTime() {
    logger.debug("getClaimedSigningTime");
    return origin.getSignedProperties().getSigningTime();
  }

  @Override
  public Date getSigningTime() {
    return getClaimedSigningTime();
  }

  @Override
  @Deprecated
  public URI getSignaturePolicyURI() {
    logger.debug("getSignaturePolicyURI");
    return null;
  }

  @Override
  public String getStateOrProvince() {
    logger.debug("getStateOrProvince");
    
    String stateOrProvince = null;
    if (origin.getSignedProperties().getSignatureProductionPlace() != null) {
      stateOrProvince = origin.getSignedProperties().getSignatureProductionPlace().getStateOrProvince();;
    }
    
    return stateOrProvince == null ? "": stateOrProvince;
  }

  @Override
  public X509Cert getTimeStampTokenCertificate() {
    logger.warn("Not yet implemented");
    throw new NotYetImplementedException();
  }

  @Override
  public SignatureValidationResult validateSignature() {
    logger.debug("");
    List<DigiDoc4JException> validationErrors = new ArrayList<>();
    ArrayList validationResult = origin.verify(origin.getSignedDoc(), true, true);
    for (Object exception : validationResult) {
      String errorMessage = exception.toString();
      logger.info(errorMessage);
      validationErrors.add(new DigiDoc4JException(errorMessage));
    }
    logger.info("Signature has " + validationErrors.size() + " validation errors");
    SignatureValidationResult result = new SignatureValidationResult();
    result.setErrors(validationErrors);
    return result;
  }

  @Override
  @Deprecated
  public List<DigiDoc4JException> validate() {
    return validateSignature().getErrors();
  }

  /**
   * Retrieves CertValue element with the desired type
   *
   * @param type CertValue type
   * @return CertValue element or null if not found
   */
  public CertValue getCertValueOfType(int type) {
    logger.debug("type: " + type);
    return origin.getCertValueOfType(type);
  }

  @Override
  public byte[] getAdESSignature() {
    logger.debug("getAdESSignature");
    return origin.getOrigContent();
  }

  @Override
  @Deprecated
  public byte[] getRawSignature() {
    return getAdESSignature();
  }

  public int getIndexInArray() {
    return indexInArray;
  }

  public void setIndexInArray(int indexInArray) {
    this.indexInArray = indexInArray;
  }
}
