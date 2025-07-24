package myproject

import chisel3._

class Adder(dataWidth: Int) extends Module {
  val io = IO(new Bundle {
    val a = Input(UInt(dataWidth.W))
    val b = Input(UInt(dataWidth.W))
    val sum = Output(UInt(dataWidth.W))
  })

  io.sum := io.a + io.b
}
