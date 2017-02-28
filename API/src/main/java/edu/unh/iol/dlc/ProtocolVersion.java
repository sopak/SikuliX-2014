package edu.unh.iol.dlc;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProtocolVersion implements Comparable<ProtocolVersion> {

   private static final Pattern VERSION_PATTERN = Pattern.compile("RFB ([0-9]{3})\\.([0-9]{3})");
   final private int majorVersion;
   final private int minorVersion;
   final private String replyCode;

   private ProtocolVersion(int majorVersion, int minorVersion, String replyCode) {
      this.majorVersion = majorVersion;
      this.minorVersion = minorVersion;
      this.replyCode = replyCode;
   }

   /**
    * Parses the VNC string into an instance of object {@link ProtocolVersion}
    * @return {@link ProtocolVersion} a ProtocolVersion instance
    */
   public static ProtocolVersion parse(String protocolString) {
      if (protocolString == null) {
         throw new IllegalArgumentException("Protocol input is NULL.");
      }

      Matcher m = VERSION_PATTERN.matcher(protocolString);
      if (m.matches()) {
         return new ProtocolVersion(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), protocolString);
      } else {
         throw new IllegalArgumentException(String.format("Invalid protocol version format: '%s'", protocolString));
      }
   }

   public int getMajorVersion() {
      return majorVersion;
   }

   public int getMinorVersion() {
      return minorVersion;
   }

   @Override
   public int compareTo(ProtocolVersion o)
   {
      if (majorVersion < o.majorVersion) {
         return -1;
      } else if (majorVersion > o.majorVersion) {
         return 1;
      }

      if (minorVersion < o.minorVersion) {
         return -1;
      } else if (minorVersion > o.minorVersion) {
         return 1;
      }

      return 0;
   }

   @Override
   public boolean equals(Object o)
   {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      ProtocolVersion that = (ProtocolVersion) o;

      if (majorVersion != that.majorVersion) return false;
      if (minorVersion != that.minorVersion) return false;
      return replyCode.equals(that.replyCode);
   }

   @Override
   public int hashCode()
   {
      int result = majorVersion;
      result = 31 * result + minorVersion;
      result = 31 * result + replyCode.hashCode();
      return result;
   }

   @Override
   public String toString() {
      return replyCode;
   }
}
