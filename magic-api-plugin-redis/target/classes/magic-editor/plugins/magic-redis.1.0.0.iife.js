var redis = function(vue) {
  "use strict";
  var MagicRedis = (request, $i, modal, JavaClass) => {
    let findResources;
    JavaClass.setExtensionAttribute("org.ssssssss.magicapi.redis.RedisModule", () => {
      var _a;
      return findResources && (((_a = findResources("redis")[0]) == null ? void 0 : _a.children) || []).filter((it) => it.key).map((it) => {
        return {
          name: it.key,
          type: "org.ssssssss.magicapi.redis.redis",
          comment: it.name
        };
      }) || [];
    });
    return {
      injectResources: (fn) => findResources = fn,
      requireScript: false,
      doTest: (info) => {
        request.sendJson("/redis/jdbc/test", info).success((res) => {
          if (res === "ok") {
            modal.alert($i("mqtt.connected"), $i("mqtt.test"));
          } else {
            modal.alert($i("mqtt.connectFailed", res), $i("mqtt.test"));
          }
        });
      }
    };
  };
  var localZhCN = {
    redis: {
      title: "Redis",
      name: "\u7F13\u5B58\u6570\u636E\u6E90",
      copySuccess: "\u590D\u5236{0}\u6210\u529F",
      copyFailed: "\u590D\u5236{0}\u5931\u8D25",
      test: "\u6D4B\u8BD5\u8FDE\u63A5",
      connected: "\u8FDE\u63A5\u6210\u529F",
      connectFailed: "\u8FDE\u63A5\u5931\u8D25\uFF0C\u9519\u8BEF\u539F\u56E0\uFF1A\r\n{0}",
      type: "\u7C7B\u578B",
      form: {
        placeholder: {
          name: "\u6570\u636E\u6E90\u540D\u79F0\uFF0C\u4EC5\u505A\u663E\u793A\u4F7F\u7528",
          key: "\u6570\u636E\u6E90Key\uFF0C\u540E\u7EED\u4EE3\u7801\u4E2D\u4F7F\u7528",
          type: "\u6A21\u5F0F\u7C7B\u578B\uFF1Asingle\u3001sentinel\u3001cluster"
        },
        type: "\u7C7B\u578B"
      }
    }
  };
  var localEn = {
    redis: {
      title: "Redis",
      name: "Redis",
      copySuccess: "Copy {0} Success",
      copyFailed: "Failed to Copy {0}",
      test: "Test",
      connected: "Connected",
      connectFailed: "Failed to Connect, Reason:\r\n{0}",
      type: "type",
      form: {
        placeholder: {
          key: "DataSource Key, Required",
          name: "DataSource Name, Only Display Use",
          type: "select model\uFF1Asingle\u3001sentinel\u3001cluster"
        },
        type: "Type"
      }
    }
  };
  const _hoisted_1 = { class: "magic-redis-info" };
  const _hoisted_2 = { class: "magic-form-row" };
  const _hoisted_3 = { class: "magic-form-row" };
  const _hoisted_4 = /* @__PURE__ */ vue.createElementVNode("label", null, "key", -1);
  const _hoisted_5 = { class: "magic-form-row" };
  const _hoisted_6 = { class: "magic-form-row" };
  const _sfc_main = {
    __name: "magic-redis-info",
    props: {
      info: Object
    },
    setup(__props) {
      const $i = vue.inject("i18n.format");
      const constants = vue.ref([
        "single",
        "sentinel",
        "cluster"
      ]);
      const properties = vue.ref(JSON.stringify(__props.info.properties || {}, null, 2));
      const handleChange = (value) => {
        if (value == "single") {
          properties.value = JSON.stringify({
            "host": "172.16.250.42",
            "port": "6060",
            "password": "1HNMDG4s5fz8",
            "database": "0",
            "pool": {
              "max-active": "100",
              "max-idle": "100",
              "min-idle": "100",
              "max-wait": "1000"
            }
          }, null, 2);
        } else if (value == "sentinel") {
          properties.value = JSON.stringify({
            master: "",
            nodes: ["ip:port", "ip:port"],
            password: "\u5BC6\u7801",
            pool: {
              "max-active": "\u6700\u5927\u6D3B\u8DC3\u8FDE\u63A5\u6570\uFF0C\u9ED8\u8BA48\u4E2A,\u5EFA\u8BAE100",
              "max-idle": "\u6700\u5927\u7A7A\u95F2\u8FDE\u63A5\u6570\uFF0C\u9ED8\u8BA48\u4E2A,\u5EFA\u8BAE100",
              "min-idle": "\u6700\u5C0F\u7A7A\u95F2\u8FDE\u63A5\u6570\uFF0C\u9ED8\u8BA40,\u5EFA\u8BAE100",
              "max-wait": "\u83B7\u53D6\u8FDE\u63A5\u7684\u6700\u5927\u7B49\u5F85\u65F6\u95F4\uFF0C\u9ED8\u8BA4-1\uFF0C\u8868\u793A\u65E0\u9650\u5236\uFF0C\u5355\u4F4D\u6BEB\u79D2\u3002\u5EFA\u8BAE1000"
            }
          }, null, 2);
        } else if (value == "cluster") {
          properties.value = JSON.stringify({
            nodes: ["ip:port", "ip:port"],
            password: "\u5BC6\u7801",
            pool: {
              "max-active": "\u6700\u5927\u6D3B\u8DC3\u8FDE\u63A5\u6570\uFF0C\u9ED8\u8BA48\u4E2A,\u5EFA\u8BAE100",
              "max-idle": "\u6700\u5927\u7A7A\u95F2\u8FDE\u63A5\u6570\uFF0C\u9ED8\u8BA48\u4E2A,\u5EFA\u8BAE100",
              "min-idle": "\u6700\u5C0F\u7A7A\u95F2\u8FDE\u63A5\u6570\uFF0C\u9ED8\u8BA40,\u5EFA\u8BAE100",
              "max-wait": "\u83B7\u53D6\u8FDE\u63A5\u7684\u6700\u5927\u7B49\u5F85\u65F6\u95F4\uFF0C\u9ED8\u8BA4-1\uFF0C\u8868\u793A\u65E0\u9650\u5236\uFF0C\u5355\u4F4D\u6BEB\u79D2\u3002\u5EFA\u8BAE1000"
            }
          }, null, 2);
        }
      };
      vue.watch(properties, (val) => {
        try {
          __props.info.properties = JSON.parse(val);
        } catch (e) {
          __props.info.properties = {};
        }
      });
      return (_ctx, _cache) => {
        const _component_magic_input = vue.resolveComponent("magic-input");
        const _component_magic_select = vue.resolveComponent("magic-select");
        const _component_magic_monaco_editor = vue.resolveComponent("magic-monaco-editor");
        return vue.openBlock(), vue.createElementBlock("div", _hoisted_1, [
          vue.createElementVNode("form", null, [
            vue.createElementVNode("div", _hoisted_2, [
              vue.createElementVNode("label", null, vue.toDisplayString(vue.unref($i)("message.name")), 1),
              vue.createVNode(_component_magic_input, {
                value: __props.info.name,
                "onUpdate:value": _cache[0] || (_cache[0] = ($event) => __props.info.name = $event),
                placeholder: vue.unref($i)("redis.form.placeholder.name")
              }, null, 8, ["value", "placeholder"])
            ]),
            vue.createElementVNode("div", _hoisted_3, [
              _hoisted_4,
              vue.createVNode(_component_magic_input, {
                value: __props.info.key,
                "onUpdate:value": _cache[1] || (_cache[1] = ($event) => __props.info.key = $event),
                placeholder: vue.unref($i)("redis.form.placeholder.name")
              }, null, 8, ["value", "placeholder"])
            ]),
            vue.createElementVNode("div", _hoisted_5, [
              vue.createElementVNode("label", null, vue.toDisplayString(vue.unref($i)("redis.form.type")), 1),
              vue.createVNode(_component_magic_select, {
                inputable: "",
                "onUpdate:value": [
                  handleChange,
                  _cache[2] || (_cache[2] = ($event) => __props.info.type = $event)
                ],
                value: __props.info.type,
                width: "100%",
                options: constants.value.map((it) => {
                  return { text: it, value: it };
                }),
                placeholder: vue.unref($i)("redis.form.placeholder.type")
              }, null, 8, ["value", "options", "placeholder"])
            ]),
            vue.createElementVNode("div", _hoisted_6, [
              vue.createElementVNode("label", null, vue.toDisplayString(vue.unref($i)("datasource.form.other")), 1),
              vue.createVNode(_component_magic_monaco_editor, {
                language: "json",
                value: properties.value,
                "onUpdate:value": _cache[3] || (_cache[3] = ($event) => properties.value = $event),
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
      var svgDom = document.getElementById("__svg__icons__dom__1765547367781__");
      if (!svgDom) {
        svgDom = document.createElementNS("http://www.w3.org/2000/svg", "svg");
        svgDom.style.position = "absolute";
        svgDom.style.width = "0";
        svgDom.style.height = "0";
        svgDom.id = "__svg__icons__dom__1765547367781__";
        svgDom.setAttribute("xmlns", "http://www.w3.org/2000/svg");
        svgDom.setAttribute("xmlns:link", "http://www.w3.org/1999/xlink");
      }
      svgDom.innerHTML = '<symbol  viewBox="0 0 200 200" id="magic-redis-redis"><circle cx="100" cy="100" r="95" fill="#fff" stroke="currentColor" stroke-width="2" /><text x="100" y="135" font-family="Arial" font-size="110" font-weight="bold" fill="#111" text-anchor="middle">\u7F13</text></symbol>';
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
        type: "redis",
        icon: "#magic-redis-redis",
        title: "Redis",
        name: i18n.format("redis.name"),
        service: MagicRedis(opt.request, i18n.format, opt.modal, opt.JavaClass),
        component: _sfc_main
      }]
    };
  };
  return index;
}(Vue);
