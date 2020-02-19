package stoner.tstomp.bean;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;

public class FrameVo {
    private String type;

    private LinkedMultiValueMap<String, String> headers;

    private String payload;

    public FrameVo() {
    }

    public FrameVo(String message) {
        if (message != null) {
            String[] split = message.split("[\n]");
            if (split.length > 1) {
                type = split[0];
                headers = new LinkedMultiValueMap<>();
                String line;
                String[] split1;
                int i = 0;
                for (; ; ) {
                    line = split[++i];
                    if (i >= split.length - 1) break;
                    if ((split1 = StringUtils.split(line, ":")) != null) {
                        headers.set(split1[0], split1[1]);
                    }
                }
                if (StringUtils.hasText(line)) {
                    int index = line.indexOf(0);
                    payload = index < 0 ? line : line.substring(0, index);
                }
            }
        }
    }

    private static boolean noU0000(String string) {
        for (char c : string.toCharArray()) {
            if (c == 0) return false;
        }
        return true;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LinkedMultiValueMap<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(LinkedMultiValueMap<String, String> headers) {
        this.headers = headers;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
