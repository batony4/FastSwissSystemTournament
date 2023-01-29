package fastSwiss.api

/**
 * Исключение выкидывается в случае, если предлагаемое изменение настроек турнира некорректно и не было применено.
 */
class IncorrectChangeException(override val message: String?) : Exception(message)
