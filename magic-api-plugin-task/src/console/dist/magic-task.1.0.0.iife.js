var __vite_style__ = document.createElement("style");
__vite_style__.innerHTML = "\n.magic-task-info[data-v-141b5617]{\n	display: flex;\n	flex-direction: column;\n	flex: 1;\n	padding: 5px;\n}\n.magic-task-info form[data-v-141b5617]{\n	display: flex;\n}\n.magic-task-info form label[data-v-141b5617]{\n	display: inline-block;\n	width: 75px;\n	height: var(--magic-input-height);\n	line-height: var(--magic-input-height);\n	font-weight: 400;\n	text-align: right;\n	padding: 0 5px;\n}\n.magic-task-info form[data-v-141b5617] .magic-checkbox{\n	width: 22px;\n	height: 22px;\n}\n.magic-task-info form[data-v-141b5617] .magic-textarea{\n	margin: 5px;\n}\n";
document.head.appendChild(__vite_style__);
var MagicTask = function(vue) {
  "use strict";
  function MagicTask2(bus, constants, $i, Message, request) {
    return {
      getIcon: (item) => ["TASK", "#9012FE"],
      name: $i("task.name"),
      language: "magicscript",
      defaultScript: `return 'Hello tiga-api-task'`,
      doTest: (opened) => {
        opened.running = true;
        const info = opened.item;
        const requestConfig = {
          baseURL: constants.SERVER_URL,
          url: "/task/execute",
          method: "POST",
          responseType: "json",
          headers: {},
          withCredentials: true
        };
        bus.$emit(Message.SWITCH_TOOLBAR, "log");
        requestConfig.headers[constants.HEADER_REQUEST_CLIENT_ID] = constants.CLIENT_ID;
        requestConfig.headers[constants.HEADER_REQUEST_SCRIPT_ID] = opened.item.id;
        requestConfig.headers[constants.HEADER_MAGIC_TOKEN] = constants.HEADER_MAGIC_TOKEN_VALUE;
        requestConfig.headers[constants.HEADER_REQUEST_BREAKPOINTS] = (opened.decorations || []).filter((it) => it.options.linesDecorationsClassName === "breakpoints").map((it) => it.range.startLineNumber).join(",");
        const fullName = opened.path();
        bus.status(`\u5F00\u59CB\u6D4B\u8BD5\u5B9A\u65F6\u4EFB\u52A1\u300C${fullName}\u300D`);
        request.sendPost("/task/execute", { id: info.id }, requestConfig).success((res) => {
          opened.running = false;
        }).end(() => {
          bus.status(`\u5B9A\u65F6\u4EFB\u52A1\u300C${fullName}\u300D\u6D4B\u8BD5\u5B8C\u6BD5`);
          opened.running = false;
        });
      },
      runnable: true,
      requirePath: true,
      merge: (item) => item
    };
  }
  var localZhCN = {
    task: {
      title: "\u5B9A\u65F6\u4EFB\u52A1\u4FE1\u606F",
      name: "\u5B9A\u65F6\u4EFB\u52A1",
      form: {
        name: "\u4EFB\u52A1\u540D\u79F0",
        path: "\u4EFB\u52A1\u8DEF\u5F84",
        placeholder: {
          cron: "\u8BF7\u8F93\u5165Cron\u8868\u8FBE\u5F0F",
          name: "\u8BF7\u8F93\u5165\u4EFB\u52A1\u540D\u79F0",
          path: "\u8BF7\u8F93\u5165\u4EFB\u52A1\u8DEF\u5F84",
          description: "\u8BF7\u8F93\u5165\u4EFB\u52A1\u63CF\u8FF0"
        }
      }
    }
  };
  var localEn = {
    task: {
      title: "Task Info",
      name: "Task",
      form: {
        name: "Task Name",
        path: "Task Path",
        placeholder: {
          cron: "Please Enter Cron Expression",
          name: "Please Enter Task Name",
          path: "Please Enter Task Path",
          description: "Please Enter Task Description"
        }
      }
    }
  };
  var magicTaskInfo_vue_vue_type_style_index_0_scoped_true_lang = "";
  var _export_sfc = (sfc, props) => {
    const target = sfc.__vccOpts || sfc;
    for (const [key, val] of props) {
      target[key] = val;
    }
    return target;
  };
  const _hoisted_1 = { class: "magic-task-info" };
  const _hoisted_2 = { style: { "flex": "1", "padding-top": "5px" } };
  const _sfc_main = {
    __name: "magic-task-info",
    setup(__props) {
      const $i = vue.inject("i18n.format");
      const info = vue.inject("info");
      return (_ctx, _cache) => {
        const _component_magic_checkbox = vue.resolveComponent("magic-checkbox");
        const _component_magic_input = vue.resolveComponent("magic-input");
        const _component_magic_textarea = vue.resolveComponent("magic-textarea");
        return vue.openBlock(), vue.createElementBlock("div", _hoisted_1, [
          vue.createElementVNode("form", null, [
            vue.createElementVNode("label", null, vue.toDisplayString(vue.unref($i)("message.enable")), 1),
            vue.createVNode(_component_magic_checkbox, {
              value: vue.unref(info).enabled,
              "onUpdate:value": _cache[0] || (_cache[0] = ($event) => vue.unref(info).enabled = $event)
            }, null, 8, ["value"]),
            _cache[5] || (_cache[5] = vue.createElementVNode("label", null, "cron", -1)),
            vue.createVNode(_component_magic_input, {
              value: vue.unref(info).cron,
              "onUpdate:value": _cache[1] || (_cache[1] = ($event) => vue.unref(info).cron = $event),
              placeholder: vue.unref($i)("task.form.placeholder.cron"),
              width: "250px"
            }, null, 8, ["value", "placeholder"]),
            vue.createElementVNode("label", null, vue.toDisplayString(vue.unref($i)("task.form.name")), 1),
            vue.createVNode(_component_magic_input, {
              value: vue.unref(info).name,
              "onUpdate:value": _cache[2] || (_cache[2] = ($event) => vue.unref(info).name = $event),
              placeholder: vue.unref($i)("task.form.placeholder.name"),
              width: "250px"
            }, null, 8, ["value", "placeholder"]),
            vue.createElementVNode("label", null, vue.toDisplayString(vue.unref($i)("task.form.path")), 1),
            vue.createVNode(_component_magic_input, {
              value: vue.unref(info).path,
              "onUpdate:value": _cache[3] || (_cache[3] = ($event) => vue.unref(info).path = $event),
              placeholder: vue.unref($i)("task.form.placeholder.path"),
              width: "auto",
              style: { "flex": "1" }
            }, null, 8, ["value", "placeholder"])
          ]),
          vue.createElementVNode("div", _hoisted_2, [
            vue.createVNode(_component_magic_textarea, {
              value: vue.unref(info).description,
              "onUpdate:value": _cache[4] || (_cache[4] = ($event) => vue.unref(info).description = $event),
              placeholder: vue.unref($i)("task.form.placeholder.description")
            }, null, 8, ["value", "placeholder"])
          ])
        ]);
      };
    }
  };
  var MagicTaskInfo = /* @__PURE__ */ _export_sfc(_sfc_main, [["__scopeId", "data-v-141b5617"]]);
  if (typeof window !== "undefined") {
    let loadSvg = function() {
      var body = document.body;
      var svgDom = document.getElementById("__svg__icons__dom__1765545977556__");
      if (!svgDom) {
        svgDom = document.createElementNS("http://www.w3.org/2000/svg", "svg");
        svgDom.style.position = "absolute";
        svgDom.style.width = "0";
        svgDom.style.height = "0";
        svgDom.id = "__svg__icons__dom__1765545977556__";
        svgDom.setAttribute("xmlns", "http://www.w3.org/2000/svg");
        svgDom.setAttribute("xmlns:link", "http://www.w3.org/1999/xlink");
      }
      svgDom.innerHTML = '<symbol class="icon" viewBox="0 0 1024 1024"  id="magic-task-task"><path d="M512.787 189.403a372.25 372.25 0 1 1-.056 744.444 372.25 372.25 0 0 1 0-744.5zm20.025 179.431h-39.936a6.694 6.694 0 0 0-6.694 6.694v228.705c0 2.194 1.013 4.162 2.756 5.4l137.414 100.234a6.637 6.637 0 0 0 9.281-1.463l23.793-32.399a6.525 6.525 0 0 0-1.518-9.224l-118.459-85.61V375.528a6.694 6.694 0 0 0-6.637-6.694zM711.287 90.125a24.805 24.805 0 0 1 0 49.61H314.232a24.805 24.805 0 0 1 0-49.61h397.111z" /></symbol>';
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
      resource: [{
        type: "task",
        icon: "#magic-task-task",
        title: "task.name",
        service: MagicTask2(opt.bus, opt.constants, i18n.format, opt.Message, opt.request)
      }],
      toolbars: [{
        type: "task",
        title: "task.title",
        icon: "parameter",
        component: MagicTaskInfo
      }]
    };
  };
  return index;
}(Vue);
