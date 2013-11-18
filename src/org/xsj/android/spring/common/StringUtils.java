package org.xsj.android.spring.common;

import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

public class StringUtils {
    public static final String EMPTY = "";
    public static final int INDEX_NOT_FOUND = -1;
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }
    public static <T extends CharSequence> T defaultIfEmpty(T str, T defaultStr) {
        return StringUtils.isEmpty(str) ? defaultStr : str;
    }
    public static String uncapitalize(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        return new StringBuilder(strLen)
            .append(Character.toLowerCase(str.charAt(0)))
            .append(str.substring(1))
            .toString();
    }
    public static String substringBefore(String str, String separator) {
        if (isEmpty(str) || separator == null) {
            return str;
        }
        if (separator.length() == 0) {
            return EMPTY;
        }
        int pos = str.indexOf(separator);
        if (pos == INDEX_NOT_FOUND) {
            return str;
        }
        return str.substring(0, pos);
    }
    
	public static<T> String join(Collection<T> list,String split){
		if(list==null || list.size() ==0){
			return null;
		}else{
			String res = null;
			for(T item : list){
				if(res==null){
					res = item.toString();
				}else{
					res += split + item.toString();
				}
			}
			return res;
		}
	}
	
	public static Object fromString(String str,Class<?> clazz) throws Exception {
		Object o=null;
		if(clazz.equals(String.class)){
			o = str;
		}else if(clazz.equals(Integer.TYPE) || clazz.equals(Integer.class)){
			o = Integer.parseInt(str);
		}else if(clazz.equals(Long.TYPE) || clazz.equals(Long.class)){
			o = Long.parseLong(str);
		}else if(clazz.equals(Float.TYPE) || clazz.equals(Float.class)){
			o = Float.parseFloat(str);
		}else if(clazz.equals(Double.TYPE) || clazz.equals(Double.class)){
			o = Double.parseDouble(str);
		}else if(clazz.equals(Boolean.TYPE) || clazz.equals(Boolean.class)){
			o = Boolean.parseBoolean(str);
		}else if(clazz.equals(Date.class)){
			o = DATE_FORMAT.parse(str);
		}else if(clazz.equals(Byte.TYPE) || clazz.equals(Byte.class)){
			o = Byte.parseByte(str);
		}else if(clazz.equals(Short.TYPE) || clazz.equals(Short.class)){
			o = Short.parseShort(str);
		}else{
			o = clazz.getConstructor(String.class).newInstance(str);
		}
		return o;
	}
	  
}
