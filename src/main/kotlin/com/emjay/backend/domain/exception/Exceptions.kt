package com.emjay.backend.domain.exception

open class DomainException(message: String) : RuntimeException(message)

class ResourceNotFoundException(message: String) : DomainException(message)

class ResourceAlreadyExistsException(message: String) : DomainException(message)

class UnauthorizedException(message: String) : DomainException(message)

class InvalidCredentialsException(message: String = "Invalid email/username or password") : DomainException(message)

class InvalidTokenException(message: String = "Invalid or expired token") : DomainException(message)

class AccountNotActiveException(message: String = "Account is not active") : DomainException(message)

class AccountNotVerifiedException(message: String = "Account is not verified") : DomainException(message)
