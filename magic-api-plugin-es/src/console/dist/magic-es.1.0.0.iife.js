var es = function(vue) {
  "use strict";
  var MagicEs = (request, $i, modal, JavaClass) => {
    let findResources;
    JavaClass.setExtensionAttribute("org.ssssssss.magicapi.elasticsearch.ESModule", () => {
      var _a;
      return findResources && (((_a = findResources("es")[0]) == null ? void 0 : _a.children) || []).filter((it) => it.key).map((it) => {
        return {
          name: it.key,
          type: "org.ssssssss.magicapi.elasticsearch.ESModule",
          comment: it.name
        };
      }) || [];
    });
    return {
      injectResources: (fn) => findResources = fn,
      requireScript: false,
      doTest: (info) => {
        request.sendJson("/es/jdbc/test", info).success((res) => {
          if (res === "ok") {
            modal.alert($i("es.connected"), $i("es.test"));
          } else {
            modal.alert($i("es.connectFailed", res), $i("es.test"));
          }
        });
      }
    };
  };
  var localZhCN = {
    es: {
      title: "Elasticsearch",
      name: "Elasticsearch\u6570\u636E\u6E90",
      copySuccess: "\u590D\u5236{0}\u6210\u529F",
      copyFailed: "\u590D\u5236{0}\u5931\u8D25",
      test: "\u6D4B\u8BD5\u8FDE\u63A5",
      connected: "\u8FDE\u63A5\u6210\u529F",
      connectFailed: "\u8FDE\u63A5\u5931\u8D25\uFF0C\u9519\u8BEF\u539F\u56E0\uFF1A\r\n{0}",
      form: {
        placeholder: {
          name: "\u6570\u636E\u6E90\u540D\u79F0\uFF0C\u4EC5\u505A\u663E\u793A\u4F7F\u7528",
          key: "\u6570\u636E\u6E90Key\uFF0C\u540E\u7EED\u4EE3\u7801\u4E2D\u4F7F\u7528"
        }
      }
    }
  };
  var localEn = {
    es: {
      title: "Elasticsearch",
      name: "Elasticsearch",
      copySuccess: "Copy {0} Success",
      copyFailed: "Failed to Copy {0}",
      test: "Test",
      connected: "Connected",
      connectFailed: "Failed to Connect, Reason:\r\n{0}",
      form: {
        placeholder: {
          key: "DataSource Key, Required",
          name: "DataSource Name, Only Display Use"
        }
      }
    }
  };
  const _hoisted_1 = { class: "magic-es-info" };
  const _hoisted_2 = { class: "magic-form-row" };
  const _hoisted_3 = { class: "magic-form-row" };
  const _hoisted_4 = /* @__PURE__ */ vue.createElementVNode("label", null, "key", -1);
  const _hoisted_5 = { class: "magic-form-row" };
  const _sfc_main = {
    __name: "magic-es-info",
    props: {
      info: Object
    },
    setup(__props) {
      const $i = vue.inject("i18n.format");
      vue.inject("constants");
      const properties = vue.ref(JSON.stringify(__props.info.properties || {
        address: "es \u96C6\u7FA4\u7684 \u5730\u5740\u5217\u8868\uFF08ip:port\uFF0C\u3002\u3002\u3002\uFF09",
        username: "\u8BA4\u8BC1\u7528\u6237\u540D\u79F0",
        password: "\u8BA4\u8BC1\u5BC6\u7801",
        connectTimeout: "\u5EFA\u7ACB\u8FDE\u63A5\u7684\u8D85\u65F6\u65F6\u95F4\uFF08\u6BEB\u79D2\uFF09\uFF0C\u5EFA\u8BAE\uFF1A5000",
        socketTimeout: "\u6570\u636E\u4F20\u8F93\u8D85\u65F6\uFF08\u6BEB\u79D2\uFF09,\u5EFA\u8BAE\uFF1A30000",
        connectionRequestTimeout: "\u4ECE\u8FDE\u63A5\u6C60\u83B7\u53D6\u8FDE\u63A5\u7684\u8D85\u65F6\uFF08\u6BEB\u79D2\uFF09,\u5EFA\u8BAE\uFF1A5000",
        maxConnTotal: "\u6700\u5927\u603B\u8FDE\u63A5\u6570\uFF08\u9ED8\u8BA420\uFF0C\u9AD8\u5E76\u53D1\u9700\u8C03\u5927\uFF09,\u5EFA\u8BAE\uFF1A30",
        maxConnPerRoute: "\u6BCF\u4E2A\u8DEF\u7531\uFF08\u8282\u70B9\uFF09\u7684\u6700\u5927\u8FDE\u63A5\u6570\uFF08\u9ED8\u8BA410\uFF0C\u9AD8\u5E76\u53D1\u9700\u8C03\u5927\uFF09,\u5EFA\u8BAE\uFF1A\u4E0A\u9762\u8FD9\u4E2A\u53C2\u6570\u7684\u4E00\u534A 15"
      }, null, 2));
      vue.watch(properties, (val) => {
        try {
          __props.info.properties = JSON.parse(val);
        } catch (e) {
          __props.info.properties = {};
        }
      });
      return (_ctx, _cache) => {
        const _component_magic_input = vue.resolveComponent("magic-input");
        const _component_magic_monaco_editor = vue.resolveComponent("magic-monaco-editor");
        return vue.openBlock(), vue.createElementBlock("div", _hoisted_1, [
          vue.createElementVNode("form", null, [
            vue.createElementVNode("div", _hoisted_2, [
              vue.createElementVNode("label", null, vue.toDisplayString(vue.unref($i)("message.name")), 1),
              vue.createVNode(_component_magic_input, {
                value: __props.info.name,
                "onUpdate:value": _cache[0] || (_cache[0] = ($event) => __props.info.name = $event),
                placeholder: vue.unref($i)("es.form.placeholder.name")
              }, null, 8, ["value", "placeholder"])
            ]),
            vue.createElementVNode("div", _hoisted_3, [
              _hoisted_4,
              vue.createVNode(_component_magic_input, {
                value: __props.info.key,
                "onUpdate:value": _cache[1] || (_cache[1] = ($event) => __props.info.key = $event),
                placeholder: vue.unref($i)("es.form.placeholder.name")
              }, null, 8, ["value", "placeholder"])
            ]),
            vue.createElementVNode("div", _hoisted_5, [
              vue.createElementVNode("label", null, vue.toDisplayString(vue.unref($i)("datasource.form.other")), 1),
              vue.createVNode(_component_magic_monaco_editor, {
                language: "json",
                value: properties.value,
                "onUpdate:value": _cache[2] || (_cache[2] = ($event) => properties.value = $event),
                style: { "height": "150px" }
              }, null, 8, ["value"])
            ])
          ])
        ]);
      };
    }
  };
  if (typeof window !== "undefined") {
    let loadSvg = function() {
      var body = document.body;
      var svgDom = document.getElementById("__svg__icons__dom__1765545955734__");
      if (!svgDom) {
        svgDom = document.createElementNS("http://www.w3.org/2000/svg", "svg");
        svgDom.style.position = "absolute";
        svgDom.style.width = "0";
        svgDom.style.height = "0";
        svgDom.id = "__svg__icons__dom__1765545955734__";
        svgDom.setAttribute("xmlns", "http://www.w3.org/2000/svg");
        svgDom.setAttribute("xmlns:link", "http://www.w3.org/1999/xlink");
      }
      svgDom.innerHTML = '<symbol  viewBox="0 0 200 200" id="magic-es-es"><circle cx="100" cy="100" r="95" fill="#fff" stroke="currentColor" stroke-width="2" /><text x="100" y="135" font-family="Arial" font-size="110" font-weight="bold" fill="#111" text-anchor="middle">\u641C</text></symbol>';
      body.insertBefore(svgDom, body.firstChild);
    };
    if (document.readyState === "loading") {
      document.addEventListener("DOMContentLoaded", loadSvg);
    } else {
      loadSvg();
    }
  }
  var index = (opt) => {
    const i18n = opt.i18n;
    i18n.add("zh-cn", localZhCN);
    i18n.add("en", localEn);
    return {
      datasources: [{
        type: "es",
        icon: "#magic-es-es",
        title: "Es",
        name: i18n.format("es.name"),
        service: MagicEs(opt.request, i18n.format, opt.modal, opt.JavaClass),
        component: _sfc_main
      }]
    };
  };
  return index;
}(Vue);
