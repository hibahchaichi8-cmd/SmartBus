export class ApiError extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}
export function errorHandler(error, req, res, next) {
  const status =
    error.status || (error.type === "entity.parse.failed" ? 400 : 500);
  res
    .status(status)
    .json({
      timestamp: new Date().toISOString(),
      status,
      code: `HTTP_${status}`,
      message: status === 500 ? "Unexpected server error" : error.message,
      path: req.path,
      errors: {},
    });
}
