package com.edumy.learning.integration;

/**
 * Port that produces the PDF bytes of a certificate from its stored snapshot.
 */
public interface CertificatePdfRenderer {

    byte[] render(CertificatePdfData certificate);
}
