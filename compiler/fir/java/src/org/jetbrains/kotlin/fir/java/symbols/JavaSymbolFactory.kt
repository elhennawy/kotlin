/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.java.symbols

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.impl.FirClassImpl
import org.jetbrains.kotlin.fir.declarations.impl.FirTypeParameterImpl
import org.jetbrains.kotlin.fir.expressions.FirAnnotationCall
import org.jetbrains.kotlin.fir.expressions.FirErrorExpression
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.impl.*
import org.jetbrains.kotlin.fir.java.FirJavaType
import org.jetbrains.kotlin.fir.resolve.FirSymbolProvider
import org.jetbrains.kotlin.fir.resolve.impl.getOrPut
import org.jetbrains.kotlin.fir.service
import org.jetbrains.kotlin.fir.symbols.ConeClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeParameterSymbol
import org.jetbrains.kotlin.fir.types.ConeClassErrorType
import org.jetbrains.kotlin.fir.types.ConeKotlinTypeProjection
import org.jetbrains.kotlin.fir.types.FirResolvedType
import org.jetbrains.kotlin.fir.types.FirType
import org.jetbrains.kotlin.fir.types.impl.ConeClassTypeImpl
import org.jetbrains.kotlin.fir.types.impl.ConeTypeParameterTypeImpl
import org.jetbrains.kotlin.fir.types.impl.FirErrorTypeImpl
import org.jetbrains.kotlin.fir.types.impl.FirResolvedTypeImpl
import org.jetbrains.kotlin.ir.expressions.IrConstKind
import org.jetbrains.kotlin.load.java.structure.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.Variance

class JavaSymbolFactory(val session: FirSession) {

    private val symbolProvider: FirSymbolProvider get() = session.service()

    val symbols = mutableMapOf<ClassId, ConeClassSymbol>()

    private val JavaClass.modality: Modality
        get() = when {
            isAbstract -> Modality.ABSTRACT
            isFinal -> Modality.FINAL
            else -> Modality.OPEN
        }

    private val JavaClass.classKind: ClassKind
        get() = when {
            isAnnotationType -> ClassKind.ANNOTATION_CLASS
            isInterface -> ClassKind.INTERFACE
            isEnum -> ClassKind.ENUM_CLASS
            else -> ClassKind.CLASS
        }

    private fun JavaClassifierType.toFirResolvedType(): FirResolvedType {
        val coneType = when (val classifier = classifier) {
            is JavaClass -> {
                val symbol = symbolProvider.getClassLikeSymbolByFqName(classifier.classId!!) as ConeClassSymbol
                ConeClassTypeImpl(symbol, typeArguments = typeArguments.map { it.toConeProjection() }.toTypedArray())
            }
            is JavaTypeParameter -> {
                // TODO: it's unclear how to identify type parameter by the symbol
                // TODO: some type parameter cache (provider?)
                val symbol = createTypeParameterSymbol(classifier.name)
                ConeTypeParameterTypeImpl(symbol)
            }
            else -> ConeClassErrorType(reason = "Unexpected classifier: $classifier")
        }
        return FirResolvedTypeImpl(
            session, psi = null, type = coneType,
            isNullable = false, annotations = annotations.map { it.toFirAnnotationCall() }
        )
    }

    private fun JavaType.toConeProjection(): ConeKotlinTypeProjection {
        if (this is JavaClassifierType) {
            return toFirResolvedType().type
        }
        return ConeClassErrorType("Unexpected type argument: $this")
    }

    private fun JavaType.toFirType(): FirType {
        return FirJavaType(session, null, this).also {
            if (this is JavaClassifierType)
                it.addAnnotationsFrom(this)
        }
    }

    private fun JavaAnnotation.toFirAnnotationCall(): FirAnnotationCall {
        return FirAnnotationCallImpl(
            session, psi = null, useSiteTarget = null,
            annotationType = FirResolvedTypeImpl(
                session = session,
                psi = null,
                type = ConeClassTypeImpl(FirClassSymbol(classId!!), emptyArray()),
                isNullable = true,
                annotations = emptyList()
            )
        ).apply {
            for (argument in this@toFirAnnotationCall.arguments) {
                arguments += argument.toFirExpression()
            }
        }
    }

    private fun JavaAnnotationArgument.toFirExpression(): FirExpression {
        // TODO: this.name
        return when (this) {
            is JavaLiteralAnnotationArgument -> when (value) {
                null -> FirConstExpressionImpl(session, null, IrConstKind.Null, null)
                else -> FirErrorExpressionImpl(session, null, "Unknown value in JavaLiteralAnnotationArgument: $value")
            }
            is JavaArrayAnnotationArgument -> FirArrayOfCallImpl(session, null).apply {
                for (element in getElements()) {
                    arguments += element.toFirExpression()
                }
            }
            // TODO
            //is JavaEnumValueAnnotationArgument -> {}
            is JavaClassObjectAnnotationArgument -> FirGetClassCallImpl(session, null).apply {
                // TODO
                //arguments += getReferencedType().toFirType()
            }
            is JavaAnnotationAsAnnotationArgument -> getAnnotation().toFirAnnotationCall()
            else -> FirErrorExpressionImpl(session, null, "Unknown JavaAnnotationArgument: ${this::class.java}")
        }
    }

    fun createClassSymbol(javaClass: JavaClass): ConeClassSymbol {
        val classId = javaClass.classId ?: error("!")

        return symbols.getOrPut(classId, {
            FirClassSymbol(classId)
        }) { firSymbol ->
            FirClassImpl(
                session, null, firSymbol, javaClass.name,
                javaClass.visibility, javaClass.modality,
                isExpect = false, isActual = false,
                classKind = javaClass.classKind,
                isInner = !javaClass.isStatic, isCompanion = false,
                isData = false, isInline = false
            ).apply {
                for (typeParameter in javaClass.typeParameters) {
                    typeParameters += createTypeParameterSymbol(typeParameter.name).fir
                }
                addAnnotationsFrom(javaClass)
                for (supertype in javaClass.supertypes) {
                    superTypes += supertype.toFirResolvedType()
                }
                // TODO: declarations (probably should be done later)
            }
        }
    }

    private fun FirAbstractAnnotatedElement.addAnnotationsFrom(javaClass: JavaAnnotationOwner) {
        for (annotation in javaClass.annotations) {
            annotations += annotation.toFirAnnotationCall()
        }
    }

    private fun createTypeParameterSymbol(name: Name): FirTypeParameterSymbol {
        val firSymbol = FirTypeParameterSymbol()
        FirTypeParameterImpl(session, null, firSymbol, name, variance = Variance.INVARIANT, isReified = false)
        return firSymbol
    }
}