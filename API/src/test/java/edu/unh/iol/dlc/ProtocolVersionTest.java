package edu.unh.iol.dlc;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProtocolVersionTest {
   @Test(expected = RuntimeException.class)
   public void throwErrorWhenNullIsPassed() {
      ProtocolVersion.parse(null);
   }

   @Test(expected = IllegalArgumentException.class)
   public void nullThrowsException() {
      ProtocolVersion.parse(null);
   }

   @Test(expected = IllegalArgumentException.class)
   public void shortThrowsException() {
      ProtocolVersion.parse("00000000");
   }

   @Test(expected = IllegalArgumentException.class)
   public void longThrowsException() {
      ProtocolVersion.parse("1234567890123");
   }

   @Test(expected = IllegalArgumentException.class)
   public void unrecognizedDigitVersionThrowsException() {
      ProtocolVersion.parse("12345678901");
   }

   @Test(expected = IllegalArgumentException.class)
   public void unrecognizedCharacterVersionThrowsException() {
      ProtocolVersion.parse("aaaaaaaaaaa");
   }

   public void unsupportedVersionThrowsException() {
      ProtocolVersion parsedVersion = ProtocolVersion.parse("RFB 005.001");
      assertEquals(5, parsedVersion.getMajorVersion());
      assertEquals(1, parsedVersion.getMinorVersion());
   }

   @Test
   public void returnThreeThree() {
      final ProtocolVersion parsedVersion = ProtocolVersion.parse("RFB 003.003");
      assertEquals(3, parsedVersion.getMajorVersion());
      assertEquals(3, parsedVersion.getMinorVersion());
   }

   @Test
   public void returnThreeFive() {
      final ProtocolVersion parsedVersion = ProtocolVersion.parse("RFB 003.005");
      assertEquals(3, parsedVersion.getMajorVersion());
      assertEquals(5, parsedVersion.getMinorVersion());
   }
}
