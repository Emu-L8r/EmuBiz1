package com.emul8r.bizap.data.network

/**
 * Sealed class for API error types.
 */
sealed class ApiException(message: String) : Exception(message) {
    class AuthenticationException(message: String) : ApiException(message)
    class PermissionException(message: String) : ApiException(message)
    class NotFoundException(message: String) : ApiException(message)
    class ClientException(message: String) : ApiException(message)
    class ServerException(message: String) : ApiException(message)
    class TimeoutException(message: String) : ApiException(message)
    class NetworkException(message: String) : ApiException(message)
    class UnknownException(message: String) : ApiException(message)
}
