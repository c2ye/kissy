/*
Copyright 2014, KISSY v1.50
MIT Licensed
build time: Apr 4 12:10
*/
/*
 Combined modules by KISSY Module Compiler: 

 component/extension/shim
*/

KISSY.add("component/extension/shim", ["ua"], function(S, require) {
  var UA = require("ua");
  var ie6 = UA.ie === 6;
  var shimTpl = "<" + 'iframe style="position: absolute;' + "border: none;" + "width: " + (ie6 ? "expression(this.parentNode.clientWidth)" : "100%") + ";" + "top: 0;" + "opacity: 0;" + "filter: alpha(opacity=0);" + "left: 0;" + "z-index: -1;" + "height: " + (ie6 ? "expression(this.parentNode.clientHeight)" : "100%") + ";" + '"/>';
  function Shim() {
  }
  Shim.ATTRS = {shim:{value:ie6}};
  Shim.prototype.__createDom = function() {
    if(this.get("shim")) {
      this.get("el").prepend(shimTpl)
    }
  };
  return Shim
});

