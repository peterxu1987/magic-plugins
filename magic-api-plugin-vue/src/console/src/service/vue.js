import { parse } from '@vue/compiler-sfc'
import { html, js, css }  from 'js-beautify'
export default function setVue(monaco) {
  // 注册 Vue 语言
  monaco.languages.register({ id: "vue" });
  
  // Vue 语法高亮配置
  monaco.languages.setMonarchTokensProvider("vue", {
    tokenPostfix: ".vue",
    keywords: [
      "template", "script", "style", "export", "default", "import",
      "setup", "defineProps", "defineEmits", "ref", "reactive", "computed",
      "v-if", "v-else", "v-for", "v-model", "v-bind", "v-on", "v-show"
    ],
    naiveUITags: [
      "n-space", "n-input", "n-button", "n-form", "n-form-item", "n-grid", "n-gi",
      "n-select", "n-option", "n-table", "n-data-table", "n-modal", "n-icon"
    ],
    elementUITags: [
      "el-button", "el-input", "el-select", "el-option", "el-table", "el-table-column",
      "el-form", "el-form-item", "el-checkbox", "el-radio", "el-switch", "el-date-picker",
      "el-dialog", "el-menu", "el-submenu", "el-menu-item", "el-tabs", "el-tab-pane"
    ],
    operators: /[=><!~?:&|+\-*\/\^%]+/,
    symbols: /[=><!~?:&|+\-*\/\^%]+/,
    escapes: /\\(?:[abfnrtv\\"']|x[0-9A-Fa-f]{1,4}|u[0-9A-Fa-f]{4}|U[0-9A-Fa-f]{8})/,
    tokenizer: {
      root: [
        // 简化标签匹配 - 避免复杂分组
        [/<(\/)?/, { token: "delimiter" }],
        
        // 标签名称
        [/[a-zA-Z_][a-zA-Z0-9_\-]*/, {
          cases: {
            "@naiveUITags": "naive.tag",
            "@elementUITags": "element.tag",
            "@keywords": "keyword",
            "@default": "tag"
          }
        }],
        
        // Vue 指令
        [/v-[a-z\-:]+/, "directive"],
        
        // 双花括号插值
        [/\{\{/, { token: "interpolation", bracket: "@open", next: "@interpolation" }],
        
        // 属性
        [/\s+[a-zA-Z_][a-zA-Z0-9_\-]*/, "attribute.name"],
        
        // 属性值
        [/="[^"]*"/, "string"],
        [/='[^']*'/, "string"],
        
        // 数字
        [/\d+\.\d+([eE][\-+]?\d+)?/, "number.float"],
        [/\d+/, "number"],
        
        // 字符串
        [/"([^"\\]|\\.)*$/, "string.invalid"],
        [/'([^'\\]|\\.)*$/, "string.invalid"],
        [/"/, "string", "@string_double"],
        [/'/, "string", "@string_single"],
        
        // 注释
        [/<!--/, "comment", "@comment"],
        [/\/\*/, "comment", "@comment_block"],
        [/\/\/.*$/, "comment"]
      ],
      
      interpolation: [
        [/\}\}/, { token: "interpolation", bracket: "@close", next: "@pop" }],
        [/.+?/, "identifier"]
      ],
      
      comment: [
        [/-->/, "comment", "@pop"],
        [/[^\-]+/, "comment"],
        [/./, "comment"]
      ],
      
      comment_block: [
        [/\*\//, "comment", "@pop"],
        [/[^*]+/, "comment"],
        [/\*/, "comment"]
      ],
      
      string_double: [
        [/[^\\"]+/, "string"],
        [/@escapes/, "string.escape"],
        [/\\./, "string.escape.invalid"],
        [/"/, "string", "@pop"]
      ],
      
      string_single: [
        [/[^\\']+/, "string"],
        [/@escapes/, "string.escape"],
        [/\\./, "string.escape.invalid"],
        [/'/, "string", "@pop"]
      ]
    }
  });


  // Vue 语言配置
  monaco.languages.setLanguageConfiguration("vue", {
    comments: {
      lineComment: "//",
      blockComment: ["/*", "*/"]
    },
    brackets: [
      ["{", "}"],
      ["[", "]"],
      ["(", ")"],
      ["<", ">"],
      ["\"", "\""],
      ["'", "'"]
    ],
    autoClosingPairs: [
      { open: "{", close: "}" },
      { open: "[", close: "]" },
      { open: "(", close: ")" },
      { open: "'", close: "'" },
      { open: '"', close: '"' },
      { open: "<", close: ">" }
    ],
    surroundingPairs: [
      { open: "{", close: "}" },
      { open: "[", close: "]" },
      { open: "(", close: ")" },
      { open: "<", close: ">" },
      { open: "'", close: "'" },
      { open: '"', close: '"' }
    ],
    folding: {
      markers: {
        start: /^\s*<!--\s*#region\b.*-->$/,
        end: /^\s*<!--\s*#endregion\b.*-->$/
      }
    }
  });

  // 注册代码提示（同时支持 Naive UI 和 Element UI）
  monaco.languages.registerCompletionItemProvider("vue", {
    triggerCharacters: ["<", " ", ":", "@", "v-", "n-", "el-"],
    provideCompletionItems: (model, position) => {
      const suggestions = [];
      const lineContent = model.getLineContent(position.lineNumber);
      const cursorPosition = position.column - 1;
      
      // Vue 模板标签提示
      const vueTags = ["template", "script", "style", "script setup"];
      vueTags.forEach(tag => {
        suggestions.push({
          label: tag === "script setup" ? "script (setup)" : tag,
          kind: monaco.languages.CompletionItemKind.Module,
          insertText: tag === "script setup" 
            ? "script setup>\n$0\n</script>" 
            : `${tag}>\n$0\n</${tag}>`,
          insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
          detail: "Vue 标签"
        });
      });
      
      // Naive UI 组件提示
      const naiveTags = [
        "n-space", "n-input", "n-button", "n-form", "n-form-item", "n-grid", "n-gi",
        "n-select", "n-option", "n-table", "n-data-table", "n-modal", "n-icon"
      ];
      
      naiveTags.forEach(tag => {
        suggestions.push({
          label: tag,
          kind: monaco.languages.CompletionItemKind.Class,
          insertText: `${tag}>\n\t$0\n</${tag}>`,
          insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
          detail: "Naive UI 组件"
        });
      });
      
      // Element UI 组件提示
      const elementTags = [
        "el-button", "el-input", "el-select", "el-option", "el-table", "el-table-column",
        "el-form", "el-form-item", "el-checkbox", "el-radio", "el-switch", "el-date-picker",
        "el-dialog", "el-menu", "el-submenu", "el-menu-item", "el-tabs", "el-tab-pane"
      ];
      
      elementTags.forEach(tag => {
        suggestions.push({
          label: tag,
          kind: monaco.languages.CompletionItemKind.Class,
          insertText: `${tag}>\n\t$0\n</${tag}>`,
          insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
          detail: "Element UI 组件"
        });
      });
      
      // Vue 指令提示
      const vueDirectives = [
        "v-if", "v-else", "v-for", "v-model", "v-bind", 
        "v-on", "v-show", "v-text", "v-html", "v-slot"
      ];
      
      vueDirectives.forEach(directive => {
        suggestions.push({
          label: directive,
          kind: monaco.languages.CompletionItemKind.Keyword,
          insertText: `${directive}="$1"`,
          insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
          detail: "Vue 指令"
        });
      });
      
      // Vue 组合式 API 提示 (在 script setup 中)
      if (lineContent.includes("<script setup>") || lineContent.includes("<script setup")) {
        const vueApis = [
          "ref", "reactive", "computed", "watch", "watchEffect",
          "defineProps", "defineEmits", "defineExpose", "useSlots", "useAttrs"
        ];
        
        vueApis.forEach(api => {
          suggestions.push({
            label: api,
            kind: monaco.languages.CompletionItemKind.Function,
            insertText: api === "defineProps" ? "defineProps<{\n\t$1\n}>()" : 
                        api === "defineEmits" ? "defineEmits<{\n\t(e: '$1', payload: any): void\n}>()" : 
                        `${api}($1)`,
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            detail: "Vue Composition API"
          });
        });
        
        // import 语句提示
        suggestions.push({
          label: "import",
          kind: monaco.languages.CompletionItemKind.Keyword,
          insertText: "import { $1 } from '$2'",
          insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
          detail: "导入模块"
        });
      }
      
      // 属性提示
      const vueAttrs = ["key", "ref", "slot", "slot-scope", "is"];
      
      vueAttrs.forEach(attr => {
        suggestions.push({
          label: attr,
          kind: monaco.languages.CompletionItemKind.Property,
          insertText: `${attr}="$1"`,
          insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
          detail: "Vue 属性"
        });
      });
      
      // UI 属性提示
      const uiAttrs = [":size", ":type", "v-model:value", "placeholder", "icon"];
      
      uiAttrs.forEach(attr => {
        suggestions.push({
          label: attr,
          kind: monaco.languages.CompletionItemKind.Property,
          insertText: attr.startsWith(":") 
            ? `${attr.slice(1)}="$1"` 
            : `${attr}="$1"`,
          insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
          detail: "UI 属性"
        });
      });
      
      return { suggestions };
    }
  });
 // 格式化函数
 monaco.languages.registerDocumentFormattingEditProvider("vue", {
    provideDocumentFormattingEdits: function (model, options, token) {
      try {
        const text = model.getValue();
        const formattedText = formatVueCode(text);
        
        return [{
          range: model.getFullModelRange(),
          text: formattedText
        }];
      } catch (error) {
        console.error("Formatting error:", error);
        return [];
      }
    }
  });

/**
 * 精准 Vue 代码格式化函数
 * @param {string} code - Vue 代码
 * @returns {string} 格式化后的代码
 */
function formatVueCode(code) {
  try {
    const { descriptor } = parse(code)
    let result = []

    // 格式化 template
    if (descriptor.template) {
      const templateContent = formatHtml(descriptor.template.content)
      result.push(`<template>${templateContent ? `\n${templateContent}\n` : ''}</template>`)
    }

    // 格式化 script
    if (descriptor.script) {
      const scriptContent = formatJs(descriptor.script.content)
      const langAttr = descriptor.script.lang ? ` lang="${descriptor.script.lang}"` : ''
      result.push(`<script${langAttr}>${scriptContent ? `\n${scriptContent}\n` : ''}</script>`)
    }

    // 格式化 scriptSetup
    if (descriptor.scriptSetup) {
      const scriptSetupContent = formatJs(descriptor.scriptSetup.content)
      const langAttr = descriptor.scriptSetup.lang ? ` lang="${descriptor.scriptSetup.lang}"` : ''
      result.push(`<script setup${langAttr}>${scriptSetupContent ? `\n${scriptSetupContent}\n` : ''}</script>`)
    }

    // 格式化 styles
    descriptor.styles.forEach(style => {
      const styleContent = formatCss(style.content)
      const scopedAttr = style.scoped ? ' scoped' : ''
      const langAttr = style.lang ? ` lang="${style.lang}"` : ''
      result.push(`<style${scopedAttr}${langAttr}>${styleContent ? `\n${styleContent}\n` : ''}</style>`)
    })

    return result.join('\n\n').trim()
  } catch (error) {
    console.error('格式化失败:', error)
    return code
  }
}

// 精准HTML格式化
function formatHtml(template) { 
  // 格式化 HTML（仅处理 <template> 内容）
  return html(template, {
    indent_size: 4,          // 缩进4空格
    wrap_attributes: 'auto', // 属性自动换行
    end_with_newline: true,  // 文件末尾加空行
  });
}

// JS格式化
function formatJs(jscode) {

    return js(jscode, {
    indent_size: 4,          // 缩进4空格
    wrap_attributes: 'auto', // 属性自动换行
    end_with_newline: true,  // 文件末尾加空行
  });
}

// CSS格式化
function formatCss(csscode) {
      return css(csscode, {
    indent_size: 4,          // 缩进4空格
    wrap_attributes: 'auto', // 属性自动换行
    end_with_newline: true,  // 文件末尾加空行
  });
}

  // 创建黑色主题
  monaco.editor.defineTheme("vue-dark", {
    base: "vs-dark",
    inherit: true,
    rules: [
      { token: "naive.tag", foreground: "E06C75", fontStyle: "bold" },
      { token: "element.tag", foreground: "61AFEF", fontStyle: "bold" },
      { token: "tag", foreground: "E5C07B" },
      { token: "directive", foreground: "C678DD", fontStyle: "bold" },
      { token: "interpolation", foreground: "56B6C2" },
      { token: "keyword", foreground: "C678DD" },
      { token: "string", foreground: "98C379" },
      { token: "number", foreground: "D19A66" },
      { token: "attribute.name", foreground: "D19A66" },
      { token: "delimiter", foreground: "ABB2BF" },
      { token: "comment", foreground: "5C6370" }
    ],
    colors: {
      "editor.background": "#1E1E1E",
      "editor.foreground": "#ABB2BF",
      "editorCursor.foreground": "#528BFF",
      "editor.lineHighlightBackground": "#2C313A",
      "editor.selectionBackground": "#3E4451",
      "editorSuggestWidget.background": "#252526",
      "editorSuggestWidget.selectedBackground": "#094771"
    }
  });

}

// 使用示例

// <script setup>
// import { ref } from 'vue'
// import { ElInput, ElButton } from 'element-plus'
// import { NSpace, NIcon } from 'naive-ui'

// const search = ref('')
// </script>`,
//   language: 'vue',
//   theme: 'vue-dark',
//   automaticLayout: true,
//   formatOnPaste: true,
//   formatOnType: true
// });