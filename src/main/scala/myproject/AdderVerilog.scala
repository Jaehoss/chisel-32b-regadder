package myproject

import chisel3._

object AdderVerilog extends App {
  emitVerilog(new Adder(32), Array("--target-dir", "./verilog"))
}
