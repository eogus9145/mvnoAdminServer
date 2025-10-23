package telecentro.common.service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import telecentro.common.dao.CommonDAO;
import telecentro.common.dto.CommonResponseDto;

@Service
@Primary
public class CommonService {
	
	private String keyStr;
	private byte[] keyBytes;
	private static byte[] iv = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
	
	@Value("${aeskey}")
	private void setValue(String value) throws UnsupportedEncodingException {
		keyStr = value;
		keyBytes = value.getBytes("UTF-8");
	}
	
	
	@Resource
	private CommonDAO commonDAO;

	// 컨트롤러 공통 응답(cd, msg, data)
	public CommonResponseDto response(String cd, Object... args) throws Exception {
	    String msg = args.length > 0 && args[0] instanceof String ? (String) args[0] : null;
	    Object data = args.length > 1 ? args[1] : null;
	    CommonResponseDto res = new CommonResponseDto(cd);
	    res.setMsg(msg);
	    res.setData(data);
	    return res;
	}
	
	// 정규식 검사
	public boolean regexCheck(String regex, String val) throws Exception {
		if(val == null || "".equals(val) || regex == null || "".equals(regex)) return false;
		return val.matches(regex);
	}
	
	
	public String generateSessionRsaKey(HttpServletRequest request) throws Exception {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048, new SecureRandom());
		KeyPair keyPair = keyPairGenerator.genKeyPair();
		PublicKey publicKey = keyPair.getPublic();
		byte[] publicKeyBytes = publicKey.getEncoded();
		String publicKeyString = Base64.getEncoder().encodeToString(publicKeyBytes);
		PrivateKey privateKey = keyPair.getPrivate();
		request.getSession().invalidate();
		request.getSession().setAttribute("rsaPrivateKey", privateKey);
		return publicKeyString;
	}
	
	
	// rsa 복호화
	public String decodeRsa(HttpSession session, String encodeStr) throws Exception {
		encodeStr = encodeStr.replace('-', '+').replace('_', '/');
		int mod = encodeStr.length() % 4;
		if(mod > 0) encodeStr += "=".repeat(4 - mod);
		byte[] cipherBytes = Base64.getDecoder().decode(encodeStr);
		PrivateKey privateKey = (PrivateKey) session.getAttribute("rsaPrivateKey");				
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(cipherBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
	}
	
	// sha512 암호화
	public String sha512Enc(String plainText, String salt) throws Exception {	
		if (plainText == null || salt == null) throw new IllegalArgumentException("평문과 솔트가 필요합니다.");
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(plainText.getBytes(StandardCharsets.UTF_8));
        md.update(salt.getBytes(StandardCharsets.UTF_8));
        byte[] hashedBytes = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hashedBytes) sb.append(String.format("%02x", b));
        return sb.toString();
	}
	
	public boolean sha512Equals(String hashText, String plainText, String salt) throws Exception {
        if (hashText == null) return false;
        String hash = hashText.trim();        
        int len = hash.length();
        if(len % 2 != 0) throw new IllegalArgumentException("유효하지 않은 해시문자열입니다.");
        byte[] stored = new byte[len / 2];
        for(int i=0; i<len; i+=2) {
        	stored[i / 2] = (byte) ((Character.digit(hash.charAt(i), 16) << 4) + Character.digit(hash.charAt(i+1), 16));
        }        
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        String text = plainText + salt;
        byte[] computed = md.digest(text.getBytes(StandardCharsets.UTF_8));
        return MessageDigest.isEqual(computed, stored);
	}
	
	public String getSalt() {
		int saltLength = 16;
		String base = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		SecureRandom random = new SecureRandom();
		StringBuilder sb = new StringBuilder(saltLength);
        for (int i = 0; i < saltLength; i++) {
            int idx = random.nextInt(base.length());
            sb.append(base.charAt(idx));
        }
        return sb.toString();
	}
		
    // AES256 인코딩(ECB)
    public String aes256Enc(String plainText) throws Exception {
    	if(plainText == null || "".equals(plainText)) return null;
    	if (plainText.startsWith("==E=%") || plainText.startsWith("=>")) return plainText;    	
    	Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encrypted);
    }
    
    // AES256 디코딩(ECB)
    public String aes256Dec(String encrypted) throws Exception {
        if (encrypted == null) return null;
        if (encrypted.startsWith("==E=%") || encrypted.startsWith("=>")) return encrypted;
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        byte[] decodedBytes = Base64.getDecoder().decode(encrypted);
        byte[] decrypted = cipher.doFinal(decodedBytes);
        return new String(decrypted, "UTF-8");
    }
	
	public long getTime(Object date) {
		return ((Timestamp) date).getTime();
	}
	
	public String setNull(String val) {
		if("".equals(val) || val.trim().length() == 0 || val == null) return null;
		else return val;
	}
	
	public String setNull(Object val) {
		if(val == "" || val == null) return null;
		String newVal = val.toString().trim();
		if(newVal.length() == 0) return null;
		return newVal;
	}
	
	public String randomNumber(int digits) {
		if(digits < 1) digits = 1;
        int max = (int) Math.pow(10, digits);
        int num = ThreadLocalRandom.current().nextInt(0, max);
        return String.format("%0" + digits + "d", num);
	}
	
	public String randomNumber() {
		return randomNumber(5);
	}
	
	public Map<String, Object> inParamMap(HttpServletRequest request, Map<String, Object> paramMap) {
		Map<String,Object> parsedMap = new HashMap<>();
        for(Map.Entry<String,Object> entry : paramMap.entrySet()) {
            String key = entry.getKey();
            Object val = entry.getValue();

            if(val instanceof String) {
                String strVal = ((String) val).trim();
                Object parsed = tryParseJson(strVal);
                parsedMap.put(key, parsed != null ? parsed : strVal);
            } else if(val instanceof String[]) {
                String[] arr = (String[]) val;
                List<Object> list = new ArrayList<>();
                for(String item : arr) {
                    Object parsed = tryParseJson(item.trim());
                    list.add(parsed != null ? parsed : item);
                }
                parsedMap.put(key, list);
            } else {
                parsedMap.put(key, val);
            }
        }
        return parsedMap;
	}
	
    private Object tryParseJson(String str) {
        if(str.startsWith("{")) {
            try {
                return new JSONObject(str).toMap(); // JSONObject → Map
            } catch(JSONException e) {
                return null;
            }
        } else if(str.startsWith("[")) {
            try {
                return toList(new JSONArray(str));
            } catch(JSONException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * JSONArray → List<Object> (재귀적으로 JSONObject도 Map으로 변환)
     */
    private List<Object> toList(JSONArray array) {
        List<Object> list = new ArrayList<>();
        for(int i=0; i<array.length(); i++) {
            Object item = array.get(i);
            if(item instanceof JSONObject) {
                list.add(((JSONObject) item).toMap());
            } else if(item instanceof JSONArray) {
                list.add(toList((JSONArray)item));
            } else {
                list.add(item);
            }
        }
        return list;
    }	
	
	public <V> Map<String, Object> selectOne(Map<String, V> param) throws Exception {
		return commonDAO.selectOne(param);
	}
	
	public <V> List<Map<String, Object>> selectList(Map<String, V> param) throws Exception {
		return commonDAO.selectList(param);
	}
	
	public <V> int selectListTotCnt(Map<String, V> param) throws Exception {
		return commonDAO.selectListTotCnt(param);
	}
	
	public <V> Map<String, Object> selectListTotSum(Map<String, V> param) throws Exception {
		return commonDAO.selectListTotSum(param);
	}
	
	public <V> int insert(Map<String, V> param) throws Exception {
		return commonDAO.insert(param);
	}
	
	public <V> int update(Map<String, V> param) throws Exception {
		return commonDAO.update(param);
	}
	public <V> int delete(Map<String, V> param) throws Exception {
		return commonDAO.delete(param);
	}
	
}
