package com.shuqiang.captain.xhs.parser;

import com.shuqiang.captain.xhs.model.XhsParseError;

/**
 * 解析链路内部异常，统一绑定错误码和用户提示。
 */
public class XhsParserException extends Exception {
    private final XhsParseError parseError;

    public XhsParserException(XhsParseError parseError) {
        super(parseError.getUserMessage());
        this.parseError = parseError;
    }

    public XhsParserException(XhsParseError parseError, Throwable cause) {
        super(parseError.getUserMessage(), cause);
        this.parseError = parseError;
    }

    public XhsParseError getParseError() {
        return parseError;
    }
}
