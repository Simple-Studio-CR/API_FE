package app.simplestudio.com.mh;

import org.springframework.stereotype.Service;

@Service
public class FuncionesService {
  public boolean isNumeric(String str) {
    for (char c : str.toCharArray()) {
      if (!Character.isDigit(c))
        return false; 
    } 
    return true;
  }
  
  public String str_pad(String input, int length, String pad, String sense) {
    int resto_pad = length - input.length();
    String padded = "";
    if (resto_pad <= 0)
      return input; 
    if (sense.equals("STR_PAD_RIGHT")) {
      padded = input;
      padded = padded + _fill_string(pad, resto_pad);
    } else if (sense.equals("STR_PAD_LEFT")) {
      padded = _fill_string(pad, resto_pad);
      padded = padded + input;
    } else {
      int pad_left = (int)Math.ceil((resto_pad / 2));
      int pad_right = resto_pad - pad_left;
      padded = _fill_string(pad, pad_left);
      padded = padded + input;
      padded = padded + _fill_string(pad, pad_right);
    } 
    return padded;
  }
  
  protected String _fill_string(String pad, int resto) {
    boolean first = true;
    String padded = "";
    if (resto >= pad.length()) {
      int i;
      for (i = resto; i >= 0; i -= pad.length()) {
        if (i >= pad.length()) {
          if (first) {
            padded = pad;
          } else {
            padded = padded + pad;
          } 
        } else if (first) {
          padded = pad.substring(0, i);
        } else {
          padded = padded + pad.substring(0, i);
        } 
        first = false;
      } 
    } else {
      padded = pad.substring(0, resto);
    } 
    return padded;
  }
}

