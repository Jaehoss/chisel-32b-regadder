package myproject

import chisel3._

class Register(dataWidth: Int) extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(dataWidth.W))
    val out = Output(UInt(dataWidth.W))
  })

  val reg = RegInit(0.U(dataWidth.W))
  reg := io.in
  io.out := reg
}
