var mqtt = function(vue) {
  "use strict";
  var MagicMqtt = (request, $i, modal, JavaClass) => {
    let findResources;
    JavaClass.setExtensionAttribute("org.ssssssss.magicapi.mqtt.MqttModule", () => {
      var _a;
      return findResources && (((_a = findResources("mqtt")[0]) == null ? void 0 : _a.children) || []).filter((it) => it.key).map((it) => {
        return {
          name: it.key,
          type: "org.ssssssss.magicapi.mqtt.MqttModule",
          comment: it.name
        };
      }) || [];
    });
    return {
      injectResources: (fn) => findResources = fn,
      requireScript: false,
      doTest: (info) => {
        request.sendJson("/mqtt/jdbc/test", info).success((res) => {
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
    mqtt: {
      title: "Mqtt",
      name: "Mqtt\u6570\u636E\u6E90",
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
    mqtt: {
      title: "Mqtt",
      name: "Mqtt",
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
  const _hoisted_1 = { class: "magic-mqtt-info" };
  const _hoisted_2 = { class: "magic-form-row" };
  const _hoisted_3 = { class: "magic-form-row" };
  const _hoisted_4 = /* @__PURE__ */ vue.createElementVNode("label", null, "key", -1);
  const _hoisted_5 = { class: "magic-form-row" };
  const _sfc_main = {
    __name: "magic-mqtt-info",
    props: {
      info: Object
    },
    setup(__props) {
      const $i = vue.inject("i18n.format");
      vue.inject("constants");
      const properties = vue.ref(JSON.stringify(__props.info.properties || {
        broker: "mqtt \u96C6\u7FA4\u7684 broker \u5730\u5740\u5217\u8868",
        username: "\u8BA4\u8BC1\u7528\u6237\u540D\u79F0",
        password: "\u8BA4\u8BC1\u5BC6\u7801",
        queueSize: "\u7528\u4E8E\u5B58\u653E\u7B49\u5F85\u6267\u884C\u7684\u4EFB\u52A1\u7684\u961F\u5217\uFF0C\u5EFA\u8BAE\uFF1A500~1000",
        "connection-timeout": "\u8FDE\u63A5\u8D85\u65F6(\u79D2)\uFF0C\u4E0D\u5B9C\u8FC7\u957F,\u5EFA\u8BAE\uFF1A30",
        waitForCompletion: "\u5BA2\u6237\u7AEF\u8FDE\u63A5broker\u8D85\u65F6(\u79D2),\u5C40\u57DF\u7F51(3~5)\u3001\u516C\u5171Wi-Fi\uFF0810\uFF09\u3001\u8DE8\u56FD\uFF0815\uFF09",
        qos: "\u670D\u52A1\u8D28\u91CF\u7B49\u7EA7, \u5EFA\u8BAE\uFF1A\u9AD8\u5E76\u53D1\u5EFA\u8BAE1\uFF0C\u5173\u952E\u4E1A\u52A1\u75282",
        "keep-alive-interval": "\u5FC3\u8DF3\u95F4\u9694 (\u79D2)\uFF0C\u4FDD\u6301\u8FDE\u63A5\u6D3B\u8DC3\u7684\u5FC3\u8DF3\u5305\u53D1\u9001\u95F4\u9694\uFF0C\u5EFA\u8BAE\u5C0F\u4E8E\u4EE3\u7406\u670D\u52A1\u5668\u7684\u8D85\u65F6\u8BBE\u7F6E\uFF0C\u5EFA\u8BAE\uFF1A120",
        "automatic-reconnect": "\u5FC5\u987B\u5F00\u542F\u81EA\u52A8\u91CD\u8FDE\uFF0Ctrue",
        "clean-session": "true: \u6BCF\u6B21\u8FDE\u63A5\u521B\u5EFA\u65B0\u4F1A\u8BDD(\u4E0D\u4FDD\u7559\u8BA2\u9605\u548C\u672A\u63A5\u6536\u6D88\u606F); false: \u6062\u590D\u5DF2\u6709\u4F1A\u8BDD(\u4FDD\u7559\u8BA2\u9605\u548CQoS1/2\u7684\u672A\u63A5\u6536\u6D88\u606F)\uFF0C\u5EFA\u8BAE true"
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
                placeholder: vue.unref($i)("mqtt.form.placeholder.name")
              }, null, 8, ["value", "placeholder"])
            ]),
            vue.createElementVNode("div", _hoisted_3, [
              _hoisted_4,
              vue.createVNode(_component_magic_input, {
                value: __props.info.key,
                "onUpdate:value": _cache[1] || (_cache[1] = ($event) => __props.info.key = $event),
                placeholder: vue.unref($i)("mqtt.form.placeholder.name")
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
      var svgDom = document.getElementById("__svg__icons__dom__1765545968046__");
      if (!svgDom) {
        svgDom = document.createElementNS("http://www.w3.org/2000/svg", "svg");
        svgDom.style.position = "absolute";
        svgDom.style.width = "0";
        svgDom.style.height = "0";
        svgDom.id = "__svg__icons__dom__1765545968046__";
        svgDom.setAttribute("xmlns", "http://www.w3.org/2000/svg");
        svgDom.setAttribute("xmlns:link", "http://www.w3.org/1999/xlink");
      }
      svgDom.innerHTML = '<symbol  viewBox="0 0 200 200" id="magic-mqtt-mqtt"><circle cx="100" cy="100" r="95" fill="#fff" stroke="currentColor" stroke-width="2" /><text x="100" y="135" font-family="Arial" font-size="110" font-weight="bold" fill="#111" text-anchor="middle">\u6D88</text></symbol>';
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
        type: "mqtt",
        icon: "#magic-mqtt-mqtt",
        title: "Mqtt",
        name: i18n.format("mqtt.name"),
        service: MagicMqtt(opt.request, i18n.format, opt.modal, opt.JavaClass),
        component: _sfc_main
      }]
    };
  };
  return index;
}(Vue);
