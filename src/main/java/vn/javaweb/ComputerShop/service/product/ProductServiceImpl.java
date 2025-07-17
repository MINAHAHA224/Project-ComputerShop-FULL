package vn.javaweb.ComputerShop.service.product;

import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.javaweb.ComputerShop.component.MessageComponent;
import vn.javaweb.ComputerShop.domain.dto.request.*;
import vn.javaweb.ComputerShop.domain.dto.response.*;
import vn.javaweb.ComputerShop.domain.entity.CartDetailEntity;
import vn.javaweb.ComputerShop.domain.entity.ProductEntity;
import vn.javaweb.ComputerShop.handleException.NotFoundException;
import vn.javaweb.ComputerShop.repository.cart.CartDetailRepository;
import vn.javaweb.ComputerShop.repository.cart.CartRepository;
import vn.javaweb.ComputerShop.repository.product.ProductRepository;
import vn.javaweb.ComputerShop.service.upload.UploadService;
import vn.javaweb.ComputerShop.domain.dto.response.ResponseBody;


@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final UploadService uploadService;
    private final MessageComponent messageComponent;


   @Override
    public ProductFilterRpDTO handleShowDataProductFilter(ProductFilterDTO productFilterDTO) {
        int page = 1;
        try {
            if (!productFilterDTO.getPage().isEmpty()) {
                page = Integer.parseInt(productFilterDTO.getPage());
            } else {
                page = 1;
            }
        } catch (Exception e) {
            page = 1;
        }
        Pageable pageable = PageRequest.of(page - 1, 7);

        ProductFilterRpDTO result = new ProductFilterRpDTO();
        Page<ProductRpDTO> listProduct = this.productRepository.findProductFilter(productFilterDTO, pageable);
        result.setListProduct(listProduct.getContent());
        result.setPage(page);
        result.setTotalPage(listProduct.getTotalPages());
        return result;
    }
    @Override
    public ProductFilterAdRpDTO handleShowDataProductAdmin(Optional<String> pageOptional) {
        ProductFilterAdRpDTO result = new ProductFilterAdRpDTO();
        int page = 1;
        try {
            if (pageOptional.isPresent()) {
                page = Integer.parseInt(pageOptional.get());
            } else {
                page = 1;
            }
        } catch (Exception e) {
            page = 1;
            // TODO: handle exception
        }
        Pageable pageable = PageRequest.of(page - 1, 5);
        Page<ProductAdRpDTO> listProducts = this.productRepository.findProducts(pageable);
        result.setListProduct(listProducts.getContent());
        result.setPage(page);
        result.setTotalPage(listProducts.getTotalPages());
        return result;
    }

    @Override
    public ProductUpdateRqDTO handleGetProductUpdate(Long id) {
        ProductEntity product = this.productRepository.findProductEntityById(id);
        if (product == null) {
            throw new NotFoundException("Product not found");
        }
        return ProductUpdateRqDTO.builder()
                .id(product.getId())
                .image(product.getImage())
                .name(product.getName())
                .factory(product.getFactory())
                .price(product.getPrice())
                .detailDesc(product.getDetailDesc())
                .shortDesc(product.getShortDesc())
                .quantity(product.getQuantity())
                .target(product.getTarget())
                .sold(product.getSold())
                .build();

    }
    @Override
    @Transactional
    public ResponseBody handleCreateProduct (ProductCreateRqDTO productCreateRqDTO , MultipartFile file){
        String avatarProduct = this.uploadService.handleUploadFile(file, "product");
        boolean checkExistName = this.productRepository.existsProductEntityByName(productCreateRqDTO.getName().trim());
        if ( checkExistName){
            return new ResponseBody(500 ,"Admin : Tên sản phẩm đã được sử dụng" );
        }

        ProductEntity newProduct = ProductEntity.builder()
                .name(productCreateRqDTO.getName().trim())
                .price(productCreateRqDTO.getPrice())
                .detailDesc(productCreateRqDTO.getDetailDesc().trim())
                .shortDesc(productCreateRqDTO.getShortDesc().trim())
                .quantity(productCreateRqDTO.getQuantity())
                .factory(productCreateRqDTO.getFactory().trim())
                .target(productCreateRqDTO.getFactory())
                .image(avatarProduct)
                .sold(0)
                .build();
        // handle save
        this.productRepository.save(newProduct);
        return new ResponseBody(200 ,"Tạo sản phẩm thành công" );
    }

    @Override
    @Transactional
    public ResponseBody handleUpdateProduct (ProductUpdateRqDTO productUpdateRqDTO , MultipartFile file){
        ProductEntity currentProduct = this.productRepository.findProductEntityById(productUpdateRqDTO.getId());
        if ( !currentProduct.getName().equals(productUpdateRqDTO.getName())){
            boolean checkExistName = this.productRepository.existsProductEntityByName(productUpdateRqDTO.getName().trim());
            if ( checkExistName){
                return new ResponseBody(500 , "Admin : Tên sản phẩm đã được sử dụng");
            }
        }

        if (file!=null && !Objects.equals(file.getOriginalFilename(), "")){
            String  avatarProduct = this.uploadService.handleUploadFile(file, "product");
            currentProduct.setImage(avatarProduct);
        }
        currentProduct.setName(productUpdateRqDTO.getName().trim());
        currentProduct.setPrice(productUpdateRqDTO.getPrice());
        currentProduct.setDetailDesc(productUpdateRqDTO.getDetailDesc().trim());
        currentProduct.setShortDesc(productUpdateRqDTO.getShortDesc().trim());
        currentProduct.setQuantity(productUpdateRqDTO.getQuantity());
        currentProduct.setFactory(productUpdateRqDTO.getFactory().trim());
        currentProduct.setTarget(productUpdateRqDTO.getFactory());
        currentProduct.setSold(productUpdateRqDTO.getSold());
        this.productRepository.save(currentProduct);

        return new ResponseBody(200 , "Admin : Cập nhật sản phẩm thành công");
    }


    @Override
    public List<ProductRpDTO> getAllProductView() {
        Pageable pageable = PageRequest.of(0, 10);
        List<ProductEntity> allProduct = this.productRepository.findAll(pageable).getContent();
        return allProduct.stream().map(
                pd -> ProductRpDTO.builder()
                        .id(pd.getId())
                        .name(pd.getName())
                        .shortDesc(pd.getShortDesc())
                        .price(pd.getPrice())
                        .image(pd.getImage())
                        .build()
        ).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ResponseBody handleDeleteProduct (Long id){
        ProductEntity product = this.productRepository.findProductEntityById(id);
        if (product == null){
            throw new NotFoundException("Product not found");
        }
        this.productRepository.deleteProductEntityById(id);
        return new ResponseBody(200 ,"Admin : Xóa sản phẩn thành công" );

    }

    @Override
    public ProductDetailRpDTO handleGetProductRpAdmin(Long id) {
        ProductEntity product = this.productRepository.findProductEntityById(id);
        if (product == null) {
            throw new NotFoundException("Product not found");
        }
        return ProductDetailRpDTO.builder()
                .id(product.getId())
                .image(product.getImage())
                .name(product.getName())
                .factory(product.getFactory())
                .price(product.getPrice())
                .detailDesc(product.getDetailDesc())
                .shortDesc(product.getShortDesc())
                .quantity(product.getQuantity())
                .target(product.getTarget())
                .sold(product.getSold())
                .build();
    }

    @Override
    public ProductDetailRpDTO handleGetProductDetail(long id) {
        ProductEntity entity = this.productRepository.findProductEntityById(id);
        if (entity == null) {
            throw new NotFoundException("Product not found");
        }
        return ProductDetailRpDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .image(entity.getImage())
                .detailDesc(entity.getDetailDesc())
                .shortDesc(entity.getShortDesc())
                .price(entity.getPrice())
                .factory(entity.getFactory())
                .quantity(entity.getQuantity())
                .build();
    }



    @Override
    @Transactional
    public ResponseBody handleConfirmCheckout(CartDetailsListDTO cartDetailsListDTO , Locale locale) {
        List<CartDetailOneRqDTO> listResult = cartDetailsListDTO.getCartDetailOne();
        listResult.forEach(cartDetailOneRqDTO -> {
            CartDetailEntity cartDetail = this.cartDetailRepository.findCartDetailEntityById(cartDetailOneRqDTO.getId());
            cartDetail.setQuantity(cartDetailOneRqDTO.getQuantity());
            this.cartDetailRepository.save(cartDetail);
        });
        return new ResponseBody(200 ,messageComponent.getLocalizedMessage("cart.checkout.confirm.success" , locale) );
    }

}
