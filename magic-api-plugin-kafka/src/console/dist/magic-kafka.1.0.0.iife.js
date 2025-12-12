var kafka = function(vue) {
  "use strict";
  var MagicKafka = (request, $i, modal, JavaClass) => {
    let findResources;
    JavaClass.setExtensionAttribute("org.ssssssss.magicapi.kafka.KafkaModule", () => {
      var _a;
      return findResources && (((_a = findResources("kafka")[0]) == null ? void 0 : _a.children) || []).filter((it) => it.key).map((it) => {
        return {
          name: it.key,
          type: "org.ssssssss.magicapi.kafka.KafkaModule",
          comment: it.name
        };
      }) || [];
    });
    return {
      injectResources: (fn) => findResources = fn,
      requireScript: false,
      doTest: (info) => {
        request.sendJson("/kafka/jdbc/test", info).success((res) => {
          if (res === "ok") {
            modal.alert($i("kafka.connected"), $i("kafka.test"));
          } else {
            modal.alert($i("kafka.connectFailed", res), $i("kafka.test"));
          }
        });
      }
    };
  };
  var localZhCN = {
    mqtt: {
      title: "Kafka",
      name: "Kafka\u6570\u636E\u6E90",
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
      title: "Kafka",
      name: "Kafka",
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
  const _hoisted_1 = { class: "magic-kafka-info" };
  const _hoisted_2 = { class: "magic-form-row" };
  const _hoisted_3 = { class: "magic-form-row" };
  const _hoisted_4 = /* @__PURE__ */ vue.createElementVNode("label", null, "key", -1);
  const _hoisted_5 = { class: "magic-form-row" };
  const _hoisted_6 = { class: "magic-form-row" };
  const _sfc_main = {
    __name: "magic-kafka-info",
    props: {
      info: Object
    },
    setup(__props) {
      const $i = vue.inject("i18n.format");
      const constants = vue.ref([
        "string",
        "byteArray"
      ]);
      const properties = vue.ref(JSON.stringify(__props.info.properties || {}, null, 2));
      const handleChange = (value) => {
        if (value == "string") {
          properties.value = JSON.stringify({
            serverConfig: "Kafka \u96C6\u7FA4\u7684 broker \u5730\u5740\u5217\u8868",
            batchSize: "\u751F\u4EA7\u8005\u6279\u91CF\u53D1\u9001\u6D88\u606F\u7684\u5B57\u8282\u6570\u4E0A\u9650\uFF0C\u6574\u6570\uFF08\u5355\u4F4D\uFF1A\u5B57\u8282\uFF09, \u9ED8\u8BA4 16384\uFF0816KB\uFF09\uFF0C 32768\uFF0832KB\uFF09\u8F83\u5408\u7406\uFF0C\u9AD8\u541E\u5410\u573A\u666F\u53EF\u589E\u81F3 65536\uFF0864KB\uFF09\uFF0C\u4F46\u4F1A\u589E\u52A0\u5EF6\u8FDF",
            bufferMemory: "\u751F\u4EA7\u8005\u7F13\u51B2\u533A\u7684\u603B\u5185\u5B58\u5927\u5C0F\uFF0C\u6574\u6570\uFF08\u5355\u4F4D\uFF1A\u5B57\u8282\uFF09\uFF0C\u63A8\u8350 67108864\uFF0864MB\uFF09\uFF0C\u9AD8\u541E\u5410\u573A\u666F\u53EF\u8BBE\u4E3A 134217728\uFF08128MB\uFF09",
            autoOffsetReset: "latest\uFF08\u9ED8\u8BA4\uFF09\uFF1A\u4ECE\u6700\u65B0\u6D88\u606F\u5F00\u59CB\u6D88\u8D39\uFF1Bearliest\uFF1A\u4ECE\u6700\u65E9\u6D88\u606F\u5F00\u59CB\u6D88\u8D39\uFF1B",
            linger: "\u7B49\u5F85xx ms\u4EE5\u586B\u5145\u6279\u6B21\uFF0C\u5EFA\u8BAE\u503C 20",
            valueDeserializer: "string"
          }, null, 2);
        } else {
          properties.value = JSON.stringify({
            serverConfig: "Kafka \u96C6\u7FA4\u7684 broker \u5730\u5740\u5217\u8868",
            batchSize: "\u751F\u4EA7\u8005\u6279\u91CF\u53D1\u9001\u6D88\u606F\u7684\u5B57\u8282\u6570\u4E0A\u9650\uFF0C\u6574\u6570\uFF08\u5355\u4F4D\uFF1A\u5B57\u8282\uFF09, \u9ED8\u8BA4 16384\uFF0816KB\uFF09\uFF0C 32768\uFF0832KB\uFF09\u8F83\u5408\u7406\uFF0C\u9AD8\u541E\u5410\u573A\u666F\u53EF\u589E\u81F3 65536\uFF0864KB\uFF09\uFF0C\u4F46\u4F1A\u589E\u52A0\u5EF6\u8FDF",
            bufferMemory: "\u751F\u4EA7\u8005\u7F13\u51B2\u533A\u7684\u603B\u5185\u5B58\u5927\u5C0F\uFF0C\u6574\u6570\uFF08\u5355\u4F4D\uFF1A\u5B57\u8282\uFF09\uFF0C\u63A8\u8350 67108864\uFF0864MB\uFF09\uFF0C\u9AD8\u541E\u5410\u573A\u666F\u53EF\u8BBE\u4E3A 134217728\uFF08128MB\uFF09",
            autoOffsetReset: "latest\uFF08\u9ED8\u8BA4\uFF09\uFF1A\u4ECE\u6700\u65B0\u6D88\u606F\u5F00\u59CB\u6D88\u8D39\uFF1Bearliest\uFF1A\u4ECE\u6700\u65E9\u6D88\u606F\u5F00\u59CB\u6D88\u8D39\uFF1B",
            linger: "\u7B49\u5F85xx ms\u4EE5\u586B\u5145\u6279\u6B21\uFF0C\u5EFA\u8BAE\u503C 20",
            valueDeserializer: "byteArray"
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
                placeholder: vue.unref($i)("kafka.form.placeholder.name")
              }, null, 8, ["value", "placeholder"])
            ]),
            vue.createElementVNode("div", _hoisted_3, [
              _hoisted_4,
              vue.createVNode(_component_magic_input, {
                value: __props.info.key,
                "onUpdate:value": _cache[1] || (_cache[1] = ($event) => __props.info.key = $event),
                placeholder: vue.unref($i)("kafka.form.placeholder.name")
              }, null, 8, ["value", "placeholder"])
            ]),
            vue.createElementVNode("div", _hoisted_5, [
              vue.createElementVNode("label", null, vue.toDisplayString(vue.unref($i)("kafka.form.type")), 1),
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
                placeholder: vue.unref($i)("kafka.form.placeholder.type")
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
      var svgDom = document.getElementById("__svg__icons__dom__1765545962421__");
      if (!svgDom) {
        svgDom = document.createElementNS("http://www.w3.org/2000/svg", "svg");
        svgDom.style.position = "absolute";
        svgDom.style.width = "0";
        svgDom.style.height = "0";
        svgDom.id = "__svg__icons__dom__1765545962421__";
        svgDom.setAttribute("xmlns", "http://www.w3.org/2000/svg");
        svgDom.setAttribute("xmlns:link", "http://www.w3.org/1999/xlink");
      }
      svgDom.innerHTML = '<symbol  viewBox="0 0 200 200" id="magic-kafka-kafka"><circle cx="100" cy="100" r="95" fill="#fff" stroke="currentColor" stroke-width="2" /><text x="100" y="135" font-family="Arial" font-size="110" font-weight="bold" fill="#111" text-anchor="middle">\u6D88</text></symbol>';
      body.insertBefore(svgDom, body.firstChild);
    };
    if (document.readyState === "loading") {
      document.addEventListener("DOMContentLoaded", loadSvg);
    } else {
      loadSvg();
    }
  }
  var kafkaIndex = (opt) => {
    const i18n = opt.i18n;
    i18n.add("zh-cn", localZhCN);
    i18n.add("en", localEn);
    return {
      datasources: [{
        type: "kafka",
        icon: "#magic-kafka-kafka",
        title: "Kafka",
        name: i18n.format("kafka.name"),
        service: MagicKafka(opt.request, i18n.format, opt.modal, opt.JavaClass),
        component: _sfc_main
      }]
    };
  };
  return kafkaIndex;
}(Vue);
