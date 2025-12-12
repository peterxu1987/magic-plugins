var hbase1 = function(vue) {
  "use strict";
  var MagicHbase1 = (request, $i, modal, JavaClass) => {
    let findResources;
    JavaClass.setExtensionAttribute("org.ssssssss.magicapi.hbase1.HbaseModule", () => {
      var _a;
      return findResources && (((_a = findResources("hbase1")[0]) == null ? void 0 : _a.children) || []).filter((it) => it.key).map((it) => {
        return {
          name: it.key,
          type: "org.ssssssss.magicapi.hbase1.HbaseModule",
          comment: it.name
        };
      }) || [];
    });
    return {
      injectResources: (fn) => findResources = fn,
      requireScript: false,
      doTest: (info) => {
        request.sendJson("/hbase1/jdbc/test", info).success((res) => {
          if (res === "ok") {
            modal.alert($i("hbase1.connected"), $i("hbase1.test"));
          } else {
            modal.alert($i("hbase1.connectFailed", res), $i("hbase1.test"));
          }
        });
      }
    };
  };
  var localZhCN = {
    hbase1: {
      title: "Hbase1",
      name: "Hbase1.x\u6570\u636E\u6E90",
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
    hbase1: {
      title: "Hbase1",
      name: "Hbase1",
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
  const _hoisted_1 = { class: "magic-hbase-info" };
  const _hoisted_2 = { class: "magic-form-row" };
  const _hoisted_3 = { class: "magic-form-row" };
  const _hoisted_4 = /* @__PURE__ */ vue.createElementVNode("label", null, "key", -1);
  const _hoisted_5 = { class: "magic-form-row" };
  const _sfc_main = {
    __name: "magic-hbase1-info",
    props: {
      info: Object
    },
    setup(__props) {
      const $i = vue.inject("i18n.format");
      vue.inject("constants");
      const properties = vue.ref(JSON.stringify(__props.info.properties || {
        zkUrls: "zookeeper\u5355\u673A\u6216\u8005\u96C6\u7FA4\u7684\u8FDE\u63A5\u5730\u5740\uFF0C\u5982\uFF1Abigdata001:2181,bigdata002:2181,bigdata003:2181",
        hbaseRootdir: "hbase \u5B58\u50A8\u5728 hadoop \u4E0A\u7684\u8DEF\u5F84\uFF0C\u5982\uFF1Ahdfs://bigdata001:9000/hbase\uFF1B \u5B58\u5728\u672C\u5730\u78C1\u76D8\uFF1Afile:///tmp/hbase-data"
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
                placeholder: vue.unref($i)("hbase1.form.placeholder.name")
              }, null, 8, ["value", "placeholder"])
            ]),
            vue.createElementVNode("div", _hoisted_3, [
              _hoisted_4,
              vue.createVNode(_component_magic_input, {
                value: __props.info.key,
                "onUpdate:value": _cache[1] || (_cache[1] = ($event) => __props.info.key = $event),
                placeholder: vue.unref($i)("hbase1.form.placeholder.name")
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
      var svgDom = document.getElementById("__svg__icons__dom__1765548488706__");
      if (!svgDom) {
        svgDom = document.createElementNS("http://www.w3.org/2000/svg", "svg");
        svgDom.style.position = "absolute";
        svgDom.style.width = "0";
        svgDom.style.height = "0";
        svgDom.id = "__svg__icons__dom__1765548488706__";
        svgDom.setAttribute("xmlns", "http://www.w3.org/2000/svg");
        svgDom.setAttribute("xmlns:link", "http://www.w3.org/1999/xlink");
      }
      svgDom.innerHTML = '<symbol  viewBox="0 0 200 200" id="magic-hbase1-hbase1"><circle cx="100" cy="100" r="95" fill="#fff" stroke="currentColor" stroke-width="2" /><text x="100" y="135" font-family="Arial" font-size="110" font-weight="bold" fill="#111" text-anchor="middle">\u5217</text></symbol>';
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
        type: "hbase1",
        icon: "#magic-hbase1-hbase1",
        title: "Hbase1.x",
        name: i18n.format("hbase1.name"),
        service: MagicHbase1(opt.request, i18n.format, opt.modal, opt.JavaClass),
        component: _sfc_main
      }]
    };
  };
  return index;
}(Vue);
