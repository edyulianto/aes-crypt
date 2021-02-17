/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.quinos.pos;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class Cryptography {
  private static final String ALGORITHM = "AES";
  
  private static final int ITERATIONS = 2;
  
  private static final byte[] keyValue = new byte[] { 
      75, 105, 109, 98, 101, 114, 108, 121, 75, 101, 
      108, 115, 101, 121, 68, 75 };
  
  public static String encrypt(String value, String salt) throws Exception {
    Key key = generateKey();
    Cipher c = Cipher.getInstance("AES");
    c.init(1, key);
    String valueToEnc = null;
    String eValue = value;
    for (int i = 0; i < 2; i++) {
      valueToEnc = salt + eValue;
      byte[] encValue = c.doFinal(valueToEnc.getBytes());
      eValue = DatatypeConverter.printBase64Binary(encValue);//    Base64.encodeBytes(encValue);
    } 
    return eValue;
  }
  
  public static String decrypt(String value, String salt) throws Exception {
    Key key = generateKey();
    Cipher c = Cipher.getInstance("AES");
    c.init(2, key);
    String dValue = null;
    String valueToDecrypt = value;
    for (int i = 0; i < 2; i++) {
      byte[] decordedValue = DatatypeConverter.parseBase64Binary(valueToDecrypt);// Base64.decode(valueToDecrypt);
      byte[] decValue = c.doFinal(decordedValue);
      dValue = (new String(decValue)).substring(salt.length());
      valueToDecrypt = dValue;
    } 
    return dValue;
  }
  
  private static Key generateKey() throws Exception {
    Key key = new SecretKeySpec(keyValue, "AES");
    return key;
  }
}
    
