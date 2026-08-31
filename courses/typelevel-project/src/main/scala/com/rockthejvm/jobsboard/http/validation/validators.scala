package com.rockthejvm.jobsboard.http.validation

import cats.data.ValidatedNel
import cats.implicits.{catsSyntaxTuple14Semigroupal, catsSyntaxTuple2Semigroupal, catsSyntaxTuple5Semigroupal, catsSyntaxValidatedId}
import com.rockthejvm.jobsboard.domain.auth.LoginInfo
import com.rockthejvm.jobsboard.domain.job.JobInfo
import com.rockthejvm.jobsboard.domain.user.NewUserInfo
import com.rockthejvm.jobsboard.domain.auth.NewPasswordInfo

import java.net.URL
import scala.util.{Failure, Success, Try}

object validators {

  sealed trait ValidationFailure(val errorMessage: String)
  // empty field, invalid url, invalid email...
  case class EmptyField(fieldName: String) extends ValidationFailure(s"'$fieldName' is empty")
  case class InvalidUrl(fieldName: String) extends ValidationFailure(s"'$fieldName' is not a valid URL")
  case class InvalidEmail(fieldName: String) extends ValidationFailure(s"'$fieldName' is not a valid email")

  type ValidationResult[A] = ValidatedNel[ValidationFailure, A]

  trait Validator[A] {
    def validate(value: A): ValidationResult[A]
  }

  // conditions
  val emailRegex =
    """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r


  def validateRequired[A](field: A, fieldName: String)(required: A => Boolean): ValidationResult[A] = {
    if(required(field)) field.validNel
    else EmptyField(fieldName).invalidNel
  }

  def validateUrl(field: String, fieldName: String): ValidationResult[String] =
    Try(URL(field).toURI()) match {
      case Success(_) => field.validNel
      case Failure(_) => InvalidUrl(fieldName).invalidNel
    }

  def validateEmail(field: String, fieldName: String): ValidationResult[String] =
    if(emailRegex.findFirstMatchIn(field).isDefined)
      field.validNel
    else
      InvalidEmail(fieldName).invalidNel

  given jobInfoValidator: Validator[JobInfo] = (jobInfo: JobInfo) => {
    val JobInfo(
        company, // should not be empty
        title, // should not be empty
        description, // should not be empty
        externalUrl, // should be a valid url
        remote,
        location, // should not be empty
        salaryLo,
        salaryHi,
        currency,
        country,
        tags,
        image,
        seniority,
        other
    ) = jobInfo

    val validCompany = validateRequired(company, "company")(_.nonEmpty)
    val validTitle = validateRequired(title, "title")(_.nonEmpty)
    val validDescription = validateRequired(description, "description")(_.nonEmpty)
    val validExternalUrl = validateUrl(externalUrl, "externalUrl")
    val validLocation = validateRequired(location, "location")(_.nonEmpty)

    (
      validCompany,
      validTitle,
      validDescription,
      validExternalUrl,
      remote.validNel,
      validLocation,
      salaryLo.validNel,
      salaryHi.validNel,
      currency.validNel,
      country.validNel,
      tags.validNel,
      image.validNel,
      seniority.validNel,
      other.validNel
    ).mapN(JobInfo.apply) // ValidatedNel[ValidationFailure, JobInfo]
  }

  // create validators for

  // loginInfo
  given loginInfoValidator: Validator[LoginInfo] = (loginInfo: LoginInfo) => {
    val validUserEmail = validateRequired(loginInfo.email, "email")(_.nonEmpty)
      .andThen(e => validateEmail(e, "email"))

    val validUserPassword = validateRequired(loginInfo.password, "password")(_.nonEmpty)
    (validUserEmail, validUserPassword).mapN(LoginInfo.apply) // Validated[..., LoginInfo]
  }

  // newUserInfo
  given newUserInfoValidator: Validator[NewUserInfo] = (newUserInfo: NewUserInfo) => {
    val validUserEmail = validateRequired(newUserInfo.email, "email")(_.nonEmpty)
      .andThen(e => validateEmail(e, "email"))

    val validUserPassword = validateRequired(newUserInfo.password, "password")(_.nonEmpty)
    // ^^ can additionally run password validation logic here

    (
      validUserEmail,
      validUserPassword,
      newUserInfo.firstName.validNel,
      newUserInfo.lastName.validNel,
      newUserInfo.company.validNel,
    ).mapN(NewUserInfo.apply)
  }

  // newPasswordInfo
  given newPasswordInfoValidator: Validator[NewPasswordInfo] = (newPasswordInfo: NewPasswordInfo) => {
    val validOldPassword = validateRequired(newPasswordInfo.oldPassword, "oldPassword")(_.nonEmpty)
    val validNewPassword = validateRequired(newPasswordInfo.newPassword, "newPassword")(_.nonEmpty)

    (
      validOldPassword,
      validNewPassword
    ).mapN(NewPasswordInfo.apply)
  }

  
}
