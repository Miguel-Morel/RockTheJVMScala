package com.rockthejvm.jobsboard.domain

object auth {

  final case class LoginInfo(
                            email: String,
                            password:String
                            )

  final case class NewPassWordInfo(
                                  oldPassword: String,
                                  newPassword: String
                                  )

}
