USE DB_ProyectoGrupo4;
GO

IF OBJECT_ID('dbo.PruebaMantenimiento') IS NULL
BEGIN
CREATE TABLE dbo.PruebaMantenimiento (
                                         Id    INT IDENTITY(1,1) PRIMARY KEY,
                                         Texto VARCHAR(100) NOT NULL
);
CREATE NONCLUSTERED INDEX IX_PruebaMantenimiento_Texto ON dbo.PruebaMantenimiento (Texto);
END
GO

-- Estadísticas desactualizadas: filas nuevas sin que nadie consulte la tabla
INSERT INTO dbo.PruebaMantenimiento (Texto)
SELECT TOP (5000) 'Fila ' + CAST(ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) AS VARCHAR(10))
FROM sys.all_objects AS a CROSS JOIN sys.all_objects AS b;
GO

-- Índice deshabilitado
ALTER INDEX IX_PruebaMantenimiento_Texto ON dbo.PruebaMantenimiento DISABLE;
GO

-- Referencia rota (SQL Server lo permite por resolución diferida de nombres)
CREATE OR ALTER PROCEDURE dbo.usp_PruebaReferenciaRota
    AS
BEGIN
SELECT * FROM dbo.TablaQueNoExiste;
END
GO