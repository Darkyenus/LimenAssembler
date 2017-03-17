package com.darkyen.limas

/**

 */
enum class LimaToken {
    SCOPE_BEGIN,
    SCOPE_END,

    LINE_COMMENT,
    BLOCK_COMMENT,
    WHITE_SPACE,

    DEFINE_MEMORY,
    DEFINE_REGISTER,
    UNDEFINE_REGISTER,

    ADDRESS_SPECIFIER,
    GROUP_SEPARATOR,
    LABEL_PREFIX,
    COMMA,

    ARRAY_BEGIN,
    ARRAY_END,

    BINARY_LITERAL,
    OCTAL_LITERAL,
    HEXADECIMAL_LITERAL,
    DECIMAL_LITERAL,

    REGISTER_LITERAL,

    IDENTIFIER,
    REGISTER_IDENTIFIER,

    UNKNOWN
}
