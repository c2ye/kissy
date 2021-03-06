/*
Copyright 2014, KISSY v1.50
MIT Licensed
build time: Apr 4 12:16
*/
/*
 Combined modules by KISSY Module Compiler: 

 editor/plugin/fore-color/cmd
*/

KISSY.add("editor/plugin/fore-color/cmd", ["../color/cmd"], function(S, require) {
  var cmd = require("../color/cmd");
  var COLOR_STYLES = {element:"span", styles:{color:"#(color)"}, overrides:[{element:"font", attributes:{color:null}}], childRule:function(el) {
    return!(el.nodeName() === "a" || el.all("a").length)
  }};
  return{init:function(editor) {
    if(!editor.hasCommand("foreColor")) {
      editor.addCommand("foreColor", {exec:function(editor, c) {
        editor.execCommand("save");
        cmd.applyColor(editor, c, COLOR_STYLES);
        editor.execCommand("save")
      }})
    }
  }}
});

