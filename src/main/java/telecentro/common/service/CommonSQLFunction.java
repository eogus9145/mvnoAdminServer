package telecentro.common.service;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class CommonSQLFunction {
	
	@Resource
	private CommonService commonService;
	
	public String format_phone(Object phone)  {
		
		if(phone == null) return null;
		
		String oldPhone = phone.toString();
		
		String newPhone = oldPhone.trim().replaceAll("-", "").replaceAll("\\+", "").replaceAll(" ", "");
		
		if(newPhone == null || "".equals(newPhone)) return oldPhone;
		if(!newPhone.matches("\\d+")) return oldPhone;
		
		String prefix = newPhone.substring(0, 2);
		int len = newPhone.length();
		if(prefix.equals("01")) {
			if(len == 10) {
				newPhone = newPhone.substring(0,3) + "-" + newPhone.substring(3,6) + "-" + right(newPhone, 4); 
			} else if(len == 11) {
				newPhone = newPhone.substring(0,3) + "-" + newPhone.substring(3,7) +"-" + right(newPhone, 4);
			} else {
				newPhone = newPhone.substring(0,3) + "-" + newPhone.substring(3);
			}			
		} else if(prefix.equals("02")) {
			if(len == 9) {
				newPhone = newPhone.substring(0,2) + "-" + newPhone.substring(2,5) + "-" + right(newPhone, 4);
			} else if(len == 10) {
				newPhone = newPhone.substring(0,2) + "-" + newPhone.substring(2,6) + "-" + right(newPhone, 4);
			} else {
				newPhone = newPhone.substring(0,2) + "-" + newPhone.substring(2);
			}			
		} else if(prefix.equals("03") || prefix.equals("04") || prefix.equals("05") || prefix.equals("06")) {
			if(len == 10) {
				newPhone = newPhone.substring(0,3) + "-" + newPhone.substring(3,6) + "-" + right(newPhone, 4);
			} else if(len == 11) {
				newPhone = newPhone.substring(0,3) + "-" + newPhone.substring(3,7) + "-" + right(newPhone, 4);
			} else {
				newPhone = newPhone.substring(0,3) + "-" + newPhone.substring(3);
			}				
		} else if(prefix.equals("07") || prefix.equals("08")) {
			if(len == 10) {
				newPhone = newPhone.substring(0,3) + "-" + newPhone.substring(3, 6) + "-" + right(newPhone, 4);
			} else if(len == 11) {
				newPhone = newPhone.substring(0,3) + "-" + newPhone.substring(3, 7) + "-" + right(newPhone, 4);
			} else {
				newPhone = newPhone.substring(0,3) + "-" + newPhone.substring(3);
			}
		} else if(prefix.equals("15") || prefix.equals("16") || prefix.equals("18")) {
			if(len == 8) {
				newPhone = newPhone.substring(0,4) + "-" + right(newPhone, 4);
			} else if(len > 8) {
				newPhone = newPhone.substring(0,4) + "-" + newPhone.substring(3);
			}				
		} else if(prefix.equals("00")) {
			if(newPhone.substring(0, 3).equals("003") || newPhone.substring(0, 3).equals("007")) {
				newPhone = newPhone.substring(0, 5) + "-" + newPhone.substring(5);
			} else {
				newPhone = newPhone.substring(0, 3) + "-" + newPhone.substring(3);
			}
		}
		return newPhone;			
	}
	
	public String format_date(Object val) throws Exception {
		return format_date_all(val, "date");
	}
	
	public String format_kr_date(Object val) throws Exception {
		return format_date_all(val, "date_kr");
	}
	
	public String format_timestamp(Object val) throws Exception {
		return format_date_all(val, "timestamp");
	}
	public String format_timestamp_kr(Object val) throws Exception {
		return format_date_all(val, "timestamp_kr");
	}
	
	public String month(Object val) throws Exception {
		return format_date_all(val, "month");
	}
	
	public String year(Object val) throws Exception {
		return format_date_all(val, "year");
	}
	
	public String format_number(Object val) {
	    if (val == null) return null;

	    Number num;
	    if (val instanceof Number) {
	        num = (Number) val;
	    } else {
	        try {
	            num = Double.parseDouble(val.toString());
	        } catch (NumberFormatException e) {
	            return val.toString();
	        }
	    }
	    
	    boolean isZero = (num instanceof BigDecimal) ? ((BigDecimal) num).compareTo(BigDecimal.ZERO) == 0 : num.doubleValue() == 0.0; 
	    if (isZero) return "0";

	    DecimalFormat numberFormat = new DecimalFormat("###,###");
	    return numberFormat.format(num);
	}
	
	public String format_ssn(Object val) {
	    if (val == null) return null;
	    
	    String newVal = val.toString();
	    int len = newVal.length();
	    if(len == 13) {
	    	newVal = newVal.substring(0, 6) + "-" + newVal.substring(6);
	    } else if(len == 10) {
	    	newVal = newVal.substring(0, 3) + "-" + newVal.substring(3, 5) + "-" + right(newVal, 5);
	    }
	    
	    return newVal;
	}

	public String format_date_all(Object val, String type) throws Exception {
		
		if(val == null) return null;
		
		LocalDateTime ldt;
		
		if(val instanceof Timestamp ts) {
			ldt = ts.toLocalDateTime();
		} else if(val instanceof Date date) {
			ldt = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		} else {
	        String s = val.toString();
	        try {
	            if (s.length() == 10) { // yyyy-MM-dd
	                ldt = LocalDate.parse(s).atStartOfDay();
	            } else {
	                ldt = LocalDateTime.parse(s.replace(' ', 'T'));
	            }
	        } catch (Exception e) {
	            return s; // 파싱 실패 시 원본 리턴
	        }
		}
		
	    String fmt = switch (type.toLowerCase()) {
	        case "date" -> "yyyy-MM-dd";
	        case "kr-date" -> "yyyy년 MM월 dd일";
	        case "timestamp" -> "yyyy-MM-dd HH:mm:ss";
	        case "kr-timestamp" -> "yyyy년 MM월 dd일 HH시 mm분 ss초";
	        case "month" -> "yyyy-MM";
	        case "year" -> "yyyy";
	        default -> "yyyy-MM-dd";
	    };
	    
	    return ldt.format(DateTimeFormatter.ofPattern(fmt));
	}
	
	public String right(String str, int n) {
		if(str == null) return null;
		return str.length() >= n ? str.substring(str.length() - n) : str;
	}
	
}
